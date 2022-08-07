package cn.edu.hubu.rpc.core.registry.nacos;


import cn.edu.hubu.rpc.core.registry.Register;
import cn.edu.hubu.rpc.core.utils.IpUtils;
import cn.edu.hubu.rpc.core.utils.RpcException;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author hxy
 * @Date 2022/4/7
 */

public class NacosRegistry implements Register {

    public static final String EXT_NAME = "NacosRegistry";

    /**
     * slf4j Logger for this class
     */
    private final static Logger logger = LoggerFactory.getLogger(NacosRegistry.class);

    private static final String  DEFAULT_NAMESPACE = "rpc";

    private NamingService namingService;

    private static Properties nacosConfig = new Properties();
    @Override
    public void start(Map<String, String> param) throws NacosException {
        String serverAddr = param.get("serverAddr");

        // valid
        if (serverAddr==null || serverAddr.trim().length()==0) {
            throw new RpcException("rpc nacos address can not be empty");
        }
        nacosConfig.put("serverAddr",serverAddr);
        nacosConfig.put("namespace", param.get("namespace")==null?DEFAULT_NAMESPACE:param.get("namespace"));
        namingService = NamingFactory.createNamingService(nacosConfig);
        logger.debug(">>>>>>>>>>> rpc nacos namingService created.");
    }

    @Override
    public void stop() throws NacosException {
        namingService.shutDown();
    }

    /**
     *
     * @param keys      service key 需要注册的
     * @param value     service value/ip:port
     * @return
     */
    @Override
    public boolean registry(Set<String> keys, String value) throws NacosException {
        if (keys==null || keys.size()==0 || value==null || value.trim().length()==0) {
            return false;
        }
        String[] split = value.split(":");
        for (String key : keys) {
            namingService.registerInstance(key,split[0],Integer.parseInt(split[1]));
        }
        logger.debug(">>>>>>>>>>> rpc all service registry success.");
        return true;
    }

    @Override
    public boolean remove(Set<String> keys, String value) throws NacosException {
        if (keys==null || keys.size()==0 || value==null || value.trim().length()==0) {
            return false;
        }
        String[] split = value.split(":");
        for (String key : keys) {
            namingService.deregisterInstance(key,split[0],Integer.parseInt(split[1]));
        }
        logger.debug(">>>>>>>>>>> rpc all service remove success.");
        return true;
    }

    @Override
    public Map<String, TreeSet<String>> discovery(Set<String> keys) throws NacosException {
        Map<String, TreeSet<String>> multService = new HashMap<>();
        for (String key : keys) {
            List<Instance> instanceList = namingService.getAllInstances(key);
            multService.put(key,instanceList.stream()
                    .map(instance -> IpUtils.getIpPort(instance.getIp(), instance.getPort()))
                    .collect(Collectors.toCollection(TreeSet::new)));
        }
        logger.debug(">>>>>>>>>>> rpc mult remote service addr get  success.");
        return multService;
    }

    @Override
    public TreeSet<String> discovery(String key) throws NacosException {
        List<Instance> instanceList = namingService.getAllInstances(key);
        TreeSet<String> serviceAddr =
                instanceList.stream()
                .map(instance ->
                        IpUtils.getIpPort(instance.getIp(), instance.getPort()
                        )
                )
                .collect(Collectors.toCollection(TreeSet::new));
        logger.debug(">>>>>>>>>>> rpc all remote service addr get success.");
        return serviceAddr;
    }

//    public static void main(String[] args) throws NacosException {
//
//        Properties properties = new Properties();
//        properties.setProperty("serverAddr", "127.0.0.1:8848");
//        properties.setProperty("namespace", "DEFAULT_NAMESPACE");
//
//        NamingService naming = NamingFactory.createNamingService(properties);
//        naming.registerInstance("nacos.test.3", "11.11.11.11", 8888);
//        naming.registerInstance("nacos.test.3", "2.2.2.2", 9977 );
//        System.out.println("----getAllInstances:"+naming.getAllInstances("nacos.test.3"));
//        try {
//            Thread.sleep(1000000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
}
