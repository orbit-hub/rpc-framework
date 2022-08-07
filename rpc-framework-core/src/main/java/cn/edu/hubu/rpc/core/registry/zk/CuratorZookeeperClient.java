package cn.edu.hubu.rpc.core.registry.zk;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.Stat;

import java.util.Collections;
import java.util.List;

/**
 * @Author hxy
 * @Date 2022/4/23
 */

public class CuratorZookeeperClient extends AbstractZookeeperClient {

    private CuratorFramework client;

    public CuratorZookeeperClient(String zkAddress) {
        this(zkAddress, null, null);
    }

    public CuratorZookeeperClient(String zkAddress, Integer baseSleepTimes, Integer maxRetryTimes) {
        super(zkAddress, baseSleepTimes, maxRetryTimes);
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(super.getBaseSleepTimes(), super.getMaxRetryTimes());
        if (client == null) {
            client = CuratorFrameworkFactory.builder()
                    .connectString(zkAddress)
                    // 会话超时
//                    .sessionTimeoutMs(5000)
                    // 重试机制，这里是超时后1000毫秒重试一次
                    .retryPolicy(retryPolicy)
                    // 名称空间，在操作节点的时候，会以这个为父节点
                    .namespace("rpc")
                    .build();
            client.start();
        }
    }

    @Override
    public CuratorFramework getClient() {
        return client;
    }

    @Override
    public void updateNodeData(String address, String data) {
        try {
            client.setData().forPath(address, data.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getNodeData(String address) {
        try {
            byte[] result = client.getData().forPath(address);
            if (result != null) {
                return new String(result);
            }
        } catch (KeeperException.NoNodeException e) {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<String> getChildrenData(String path) {
        try {
            List<String> childrenData = client.getChildren().forPath(path);
            return childrenData;
        } catch (KeeperException.NoNodeException e) {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void createPersistentData(String address, String data) {
        try {
            client.create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT).forPath(address, data.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createPersistentWithSeqData(String address, String data) {
        try {
            client.create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT_SEQUENTIAL).forPath(address, data.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createTemporarySeqData(String address, String data) {
        try {
            client.create().creatingParentContainersIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(address, data.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createTemporaryData(String address, String data) {
        try {
            client.create().creatingParentContainersIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(address, data.getBytes());
        } catch (KeeperException.NoChildrenForEphemeralsException e) {
            try {
                client.setData().forPath(address, data.getBytes());
            } catch (Exception ex) {
                throw new IllegalStateException(ex.getMessage(), ex);
            }
        } catch (Exception ex) {
            throw new IllegalStateException(ex.getMessage(), ex);
        }
    }

    @Override
    public void setTemporaryData(String address, String data) {
        try {
            client.setData().forPath(address, data.getBytes());
        } catch (Exception ex) {
            throw new IllegalStateException(ex.getMessage(), ex);
        }
    }

    @Override
    public void destroy() {
        client.close();
    }

    @Override
    public List<String> listNode(String address) {
        try {
            return client.getChildren().forPath(address);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    @Override
    public boolean deleteNode(String address) {
        try {
            client.delete().forPath(address);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean existNode(String address) {
        try {
            Stat stat = client.checkExists().forPath(address);
            return stat != null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void watchNodeData(String path, Watcher watcher) {
        try {
            client.getData().usingWatcher(watcher).forPath(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void watchChildNodeData(String path, Watcher watcher) {
        try {
            client.getChildren().usingWatcher(watcher).forPath(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        AbstractZookeeperClient abstractZookeeperClient = new CuratorZookeeperClient("192.168.241.134:2181");
        boolean r = abstractZookeeperClient.existNode("/cn.edu.hubu.HelloService/provider/192.168.56.1:9999");
//        r.forEach(System.out::println);
        abstractZookeeperClient.deleteNode("/cn.edu.hubu.HelloService/provider/192.168.56.1:9999");
        System.out.println(r);
        //        abstractZookeeperClient.createPersistentData("/cn","192.168.56.1:9092");
        /*        abstractZookeeperClient.watchNodeData("/rpc/rpc.framework.interfaces.DataService/provider/10.1.21.11:9092",
                new Watcher() {
                    @Override
                    public void process(WatchedEvent watchedEvent) {
                        System.out.println(watchedEvent.getType());
                        if(NodeDeleted.equals(watchedEvent.getType())){
//                            ProviderNodeInfo providerNodeInfo = URL.buildURLFromUrlStr(watchedEvent.getPath());
//                            System.out.println(providerNodeInfo);
                        }
                    }
                });*/
        while (true){

        }
    }
}
