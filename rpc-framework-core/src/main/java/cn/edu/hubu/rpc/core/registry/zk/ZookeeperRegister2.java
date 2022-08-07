package cn.edu.hubu.rpc.core.registry.zk;


import cn.edu.hubu.rpc.core.registry.Register;
import cn.edu.hubu.rpc.core.utils.CommonUtil;
import cn.edu.hubu.rpc.core.utils.RpcException;

import java.util.*;

/**
 * @Author hxy
 * @Date 2022/4/23
 */

public class ZookeeperRegister2 implements Register {

    private AbstractZookeeperClient zkClient;

    public AbstractZookeeperClient getZkClient() {
        return zkClient;
    }

    public void setZkClient(AbstractZookeeperClient zkClient) {
        this.zkClient = zkClient;
    }

    @Override
    public void start(Map<String, String> param){
        String serverAddr = param.get("serverAddr");
        // valid
        if (serverAddr==null || serverAddr.trim().length()==0) {
            throw new RpcException("rpc zkaddress can not be empty");
        }
        this.zkClient = new CuratorZookeeperClient(serverAddr);

    }


    @Override
    public void stop(){
        if (zkClient!=null) {
            zkClient.destroy();
        }
    }

    @Override
    public boolean registry(Set<String> keys, String value){
        for (String key : keys) {
            String nodePath = CommonUtil.buildProviderNode(key);
            zkClient.createTemporaryData(nodePath.concat("/").concat(value),value);
        }
        return true;
    }

    @Override
    public boolean remove(Set<String> keys, String value){
        for (String key : keys) {
            String nodePath = CommonUtil.buildProviderNode(key);
            if (zkClient.existNode(nodePath)) {
                zkClient.deleteNode(nodePath);
            }
        }
        return true;
    }

    @Override
    public Map<String, TreeSet<String>> discovery(Set<String> keys){
        Map<String, TreeSet<String>> services = new HashMap<>();
        for (String key : keys) {
            String nodePath = CommonUtil.buildProviderNode(key);

            List<String> nodes = zkClient.listNode(nodePath);
            services.put(key, new TreeSet<>(nodes));
        }
        return services;
    }

    @Override
    public TreeSet<String> discovery(String key){
        String nodePath = CommonUtil.buildProviderNode(key);
        List<String> nodes = zkClient.listNode(nodePath);
        return new TreeSet<>(nodes);
    }


/*    private String getProviderPath(URL url) {
        return ROOT + "/" + url.getServiceName() + "/provider/" + url.getParameters().get("host") + ":" + url.getParameters().get("port");
    }

    private String getConsumerPath(URL url) {
        return ROOT + "/" + url.getServiceName() + "/consumer/" + url.getApplicationName() + ":" + url.getParameters().get("host") + ":";
    }*/
}
