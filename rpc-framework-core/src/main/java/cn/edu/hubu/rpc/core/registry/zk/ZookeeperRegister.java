package cn.edu.hubu.rpc.core.registry.zk;


import cn.edu.hubu.rpc.core.registry.Register;
import cn.edu.hubu.rpc.core.utils.RpcException;
import com.alibaba.nacos.api.exception.NacosException;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * @Author hxy
 * @Date 2022/4/13
 */

public class ZookeeperRegister implements Register {
    private final static Logger logger = LoggerFactory.getLogger(ZookeeperRegister.class);

    // config
    private static final String zkBasePath = "/rpc";
    private ZookeeperClient zkClient;
    private String zkEnvPath;

    private Thread refreshThread;
    private boolean refreshThreadStop = false;

    private volatile ConcurrentMap<String, TreeSet<String>> registryData = new ConcurrentHashMap<String, TreeSet<String>>();
    private volatile ConcurrentMap<String, TreeSet<String>> discoveryData = new ConcurrentHashMap<String, TreeSet<String>>();

    public ZookeeperClient getZkClient() {
        return zkClient;
    }

    public void setZkClient(ZookeeperClient zkClient) {
        this.zkClient = zkClient;
    }

    /**
     * key 2 path
     * @param   nodeKey
     * @return  znodePath
     */
    public String keyToPath(String nodeKey){
        return zkEnvPath + "/" + nodeKey;
    }

    /**
     * path 2 key
     * @param   nodePath
     * @return  nodeKey
     */
    public String pathToKey(String nodePath){
        if (nodePath==null || nodePath.length() <= zkEnvPath.length() || !nodePath.startsWith(zkEnvPath)) {
            return null;
        }
        return nodePath.substring(zkEnvPath.length()+1, nodePath.length());
    }

    @Override
    public void start(Map<String, String> param) throws NacosException {
        String serverAddr = param.get("serverAddr");
        String env = param.get("env");
        // valid
        if (serverAddr==null || serverAddr.trim().length()==0) {
            throw new RpcException("rpc zkaddress can not be empty");
        }
        if (env!=null){
            zkEnvPath= zkBasePath.concat("/").concat(env);
        }else {zkEnvPath= zkBasePath;}


        this.zkClient = new ZookeeperClient(serverAddr, zkEnvPath, "", new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                try {
                    logger.debug(">>>>>>>>>> rpc: watcher:{}", watchedEvent);

                    // session expire, close old and create new
                    if (watchedEvent.getState() == Event.KeeperState.Expired) {
                        zkClient.destroy();
                        zkClient.getClient();

                        // refreshDiscoveryData (all)：expire retry
                        refreshDiscoveryData(null);

                        logger.info(">>>>>>>>>> rpc, zk re-connect reloadAll success.");
                    }

                    // watch + refresh
                    String path = watchedEvent.getPath();
                    String key = pathToKey(path);
                    if (key != null) {
                        // keep watch conf key：add One-time trigger
                        zkClient.getClient().exists(path, true);

                        // refresh
                        if (watchedEvent.getType() == Event.EventType.NodeChildrenChanged) {
                            // refreshDiscoveryData (one)：one change
                            refreshDiscoveryData(key);
                        } else if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
                            logger.info("reload all 111");
                        }
                    }

                } catch (KeeperException e) {
                    logger.error(e.getMessage(), e);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        });

        zkClient.getClient();
        // refresh thread
        refreshThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!refreshThreadStop) {
                    try {
                        TimeUnit.SECONDS.sleep(60);

                        // refreshDiscoveryData (all)：cycle check
                        refreshDiscoveryData(null);

                        // refresh RegistryData
                        refreshRegistryData();
                    } catch (Exception e) {
                        if (!refreshThreadStop) {
                            logger.error(">>>>>>>>>> rpc, refresh thread error.", e);
                        }
                    }
                }
                logger.info(">>>>>>>>>> rpc, refresh thread stoped.");
            }
        });
        refreshThread.setName("rpc, ZkServiceRegistry refresh thread.");
        refreshThread.setDaemon(true);
        refreshThread.start();

        logger.info(">>>>>>>>>> rpc, ZkServiceRegistry init success. [env={}]", env);
    }

    @Override
    public void stop() {
        if (zkClient!=null) {
        this.zkClient.destroy();
        }
    }

    @Override
    public boolean registry(Set<String> keys, String value) {

        if (keys==null || keys.size()==0 || value==null || value.trim().length()==0) {
            return false;
        }

        for (String key : keys) {
            // local cache
            TreeSet<String> values = registryData.get(key);
            if (values == null) {
                values = new TreeSet<>();
                registryData.put(key, values);
            }
            values.add(value);
            String path = keyToPath(key);
            zkClient.setChildPathData(path,value,"");
            logger.debug(">>>>>>>>>> rpc, registry success, key = {}, value = {}", key, value);
        }
        return true;
    }

    @Override
    public boolean remove(Set<String> keys, String value) {
        for (String key : keys) {
            TreeSet<String> values = discoveryData.get(key);
            if (values != null) {
                values.remove(value);
            }
            String path = keyToPath(key);
            zkClient.deleteChildPath(path, value);
        }

        return false;
    }

    @Override
    public Map<String, TreeSet<String>> discovery(Set<String> keys){
        Map<String, TreeSet<String>> multService = new HashMap<>();
        for (String key : keys) {
            // local cache
            TreeSet<String> values = discoveryData.get(key);
            if (values == null) {
                refreshDiscoveryData(key);
                values = discoveryData.get(key);
            }
            multService.put(key,values);
        }
        logger.debug(">>>>>>>>>>> rpc mult remote service addr get  success.");
        return multService;
    }

    @Override
    public TreeSet<String> discovery(String key) {
        // local cache
        TreeSet<String> values = discoveryData.get(key);
        if (values == null) {

            // refreshDiscoveryData (one)：first use
            refreshDiscoveryData(key);

            values = discoveryData.get(key);
        }
        return values;
    }

    /**
     * refresh registry data
     */
    private void refreshRegistryData(){
        if (registryData.size() > 0) {
            for (Map.Entry<String, TreeSet<String>> item: registryData.entrySet()) {
                String key = item.getKey();
                for (String value:item.getValue()) {
                    // make path, child path
                    String path = keyToPath(key);
                    zkClient.setChildPathData(path, value, "");
                }
            }
            logger.info(">>>>>>>>>> rpc, refresh registry data success, registryData = {}", registryData);
        }
    }
    /**
     * refresh discovery data, and cache
     *
     * @param key
     */
    private void refreshDiscoveryData(String key){

        Set<String> keys = new HashSet<String>();
        if (key!=null && key.trim().length()>0) {
            keys.add(key);
        } else {
            if (discoveryData.size() > 0) {
                keys.addAll(discoveryData.keySet());
            }
        }

        if (keys.size() > 0) {
            for (String keyItem: keys) {

                // add-values
                String path = keyToPath(keyItem);
                Map<String, String> childPathData = zkClient.getChildPathData(path);

                // exist-values
                TreeSet<String> existValues = discoveryData.get(keyItem);
                if (existValues == null) {
                    existValues = new TreeSet<String>();
                    discoveryData.put(keyItem, existValues);
                }

                if (childPathData.size() > 0) {
                    existValues.addAll(childPathData.keySet());
                }
            }
            logger.info(">>>>>>>>>> rpc, refresh discovery data success, discoveryData = {}", discoveryData);
        }
    }

}
