package cn.edu.hubu.rpc.core.remoting.server;


import cn.edu.hubu.rpc.core.extension.ExtensionLoader;
import cn.edu.hubu.rpc.core.registry.Register;
import cn.edu.hubu.rpc.core.remoting.message.RpcRequestMessage;
import cn.edu.hubu.rpc.core.remoting.message.RpcResponseMessage;
import cn.edu.hubu.rpc.core.remoting.params.BaseCallback;
import cn.edu.hubu.rpc.core.remoting.server.netty_http.NettyHttpServer;
import cn.edu.hubu.rpc.core.serialize.Serializer;
import cn.edu.hubu.rpc.core.serialize.impl.HessianSerializer;
import cn.edu.hubu.rpc.core.utils.IpUtils;
import cn.edu.hubu.rpc.core.utils.NetUtil;
import cn.edu.hubu.rpc.core.utils.RpcException;
import cn.edu.hubu.rpc.core.utils.ThrowableUtil;
import com.alibaba.nacos.api.exception.NacosException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author hxy
 * @Date 2022/4/11
 */

public class RpcProviderFactory {
    private static final Logger logger = LoggerFactory.getLogger(RpcProviderFactory.class);

    // ---------------------- config ----------------------

    private Class<? extends Server> server = NettyHttpServer.class;
    private Class<? extends Serializer> serializer = HessianSerializer.class;

    //
    private int corePoolSize = 60;
    private int maxPoolSize = 300;

    private String ip = null;					// server ip, for registry
    private int port = 9999;					// server default port
    private String registryAddress;				// default use registryAddress to registry , otherwise use ip:port if registryAddress is null
    private String accessToken = null;

    private Class<? extends Register> serviceRegistry = null;
    private Map<String, String> serviceRegistryParam = null;

    // set
    public void setServer(Class<? extends Server> server) {
        this.server = server;
    }
    public void setSerializer(Class<? extends Serializer> serializer) {
        this.serializer = serializer;
    }
    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }
    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }
    public void setIp(String ip) {
        this.ip = ip;
    }
    public void setPort(int port) {
        this.port = port;
    }
    public void setRegistryAddress(String registryAddress) {
        this.registryAddress = registryAddress;
    }
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    public void setServiceRegistry(Class<? extends Register> serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public void setServiceRegistryParam(Map<String, String> serviceRegistryParam) {
        this.serviceRegistryParam = serviceRegistryParam;
    }

    // get
    public Serializer getSerializerInstance() {
        return serializerInstance;
    }
    public int getPort() {
        return port;
    }
    public int getCorePoolSize() {
        return corePoolSize;
    }
    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    // ---------------------- start / stop ----------------------

    private Server serverInstance;
    private Serializer serializerInstance;
    private Register registerInstance;


    /**
     * rpc server start
     * @throws Exception
     */
    public void start() throws Exception {

        // valid
        if (this.server == null) {
            throw new RpcException("rpc provider server missing.");
        }
        if (this.serializer==null) {
            throw new RpcException("rpc provider serializer missing.");
        }
        if (!(this.corePoolSize>0 && this.maxPoolSize>0 && this.maxPoolSize>=this.corePoolSize)) {
            this.corePoolSize = 60;
            this.maxPoolSize = 300;
        }
        if (this.ip == null) {
            this.ip = IpUtils.getIp();
        }
        if (this.port <= 0) {
            this.port = 7080;
        }
        if (this.registryAddress==null || this.registryAddress.trim().length()==0) {
            this.registryAddress = IpUtils.getIpPort(this.ip, this.port);
        }
        if (NetUtil.isPortUsed(this.port)) {
            throw new RpcException("rpc provider port["+ this.port +"] is used.");
        }

        // init serializerInstance
        this.serializerInstance = serializer.newInstance();
        // start server
        serverInstance = server.newInstance();
        //注册到注册中心
        serverInstance.setStartedCallback(new BaseCallback() {      // serviceRegistry started
            @Override
            public void run() throws Exception {
                // start registry
                if (serviceRegistry != null) {
                    registerInstance = ExtensionLoader
                            .getExtensionLoader(Register.class)
                            .getExtension("zk");
                    registerInstance.start(serviceRegistryParam);
                    if (serviceData.size() > 0) {
                        //注册接口
                        registerInstance.registry(serviceData.keySet(), registryAddress);
                    }
                }
            }

         });
        //netty服务器关闭时 注销
        serverInstance.setStopedCallback(new BaseCallback() {		// serviceRegistry stoped
            @Override
            public void run() throws NacosException {
                // stop registry
                if (registerInstance != null) {
                    if (serviceData.size() > 0) {
                        registerInstance.remove(serviceData.keySet(), registryAddress);
                    }
                    registerInstance.stop();
                    registerInstance = null;
                }
            }
        });
        serverInstance.start(this);
    }
    public void  stop() throws Exception {
        // stop server
        serverInstance.stop();
    }

    // ---------------------- server invoke ----------------------
    /**
     * init local rpc service map
     */
    private Map<String, Object> serviceData = new HashMap<String, Object>();
    public Map<String, Object> getServiceData() {
        return serviceData;
    }



    /**
     * add service
     *
     * @param intface
     * @param version
     * @param serviceBean
     */
    public void addService(String intface, String version, Object serviceBean){
        String serviceKey = makeServiceKey(intface, version);
        //serviceKey 注册中心的服务名 默认为接口的全类名+版本号
        serviceData.put(serviceKey, serviceBean);

        logger.info(">>>>>>>>>>> rpc, provider factory add service success. serviceKey = {}, serviceBean = {}", serviceKey, serviceBean.getClass());
    }



    /**
     * 服务提供方 根据请求进行本地服务调用
     * @param rpcRequestMessage
     * @return
     */
    public RpcResponseMessage invokeService(RpcRequestMessage rpcRequestMessage) {

        //  make response
        RpcResponseMessage rpcResponseMessage = new RpcResponseMessage();
        rpcResponseMessage.setRequestId(rpcRequestMessage.getRequestId());

        // match service bean
        String serviceKey = makeServiceKey(rpcRequestMessage.getInterfaceName(), rpcRequestMessage.getVersion());
        Object serviceBean = serviceData.get(serviceKey);
        // valid
        if (serviceBean == null) {
            rpcResponseMessage.setMessage("The serviceKey["+ serviceKey +"] not found.");
            return rpcResponseMessage;
        }

        if (System.currentTimeMillis() - rpcRequestMessage.getCreateMillisTime() > 3*60*1000) {
            rpcResponseMessage.setErrorMsg("The timestamp difference between admin and executor exceeds the limit.");
            return rpcResponseMessage;
        }
        if (accessToken!=null && accessToken.trim().length()>0 && !accessToken.trim().equals(rpcRequestMessage.getAccessToken())) {
            rpcResponseMessage.setErrorMsg("The access token[" + rpcRequestMessage.getAccessToken() + "] is wrong.");
            return rpcResponseMessage;
        }
        try {
            //invoke
            Class<?> serviceClass = serviceBean.getClass();
            String methodName = rpcRequestMessage.getMethodName();
            Class<?>[] parameterTypes = rpcRequestMessage.getParameterTypes();
            Object[] parameters = rpcRequestMessage.getParameterValue();

            Method method = serviceClass.getMethod(methodName, parameterTypes);
            method.setAccessible(true);
            Object result = method.invoke(serviceBean, parameters);

            rpcResponseMessage.setData(result);
        } catch (Throwable t) {
            // catch error
            logger.error("rpc provider invokeService error.", t);
            rpcResponseMessage.setErrorMsg(ThrowableUtil.toString(t));
        }

        return rpcResponseMessage;
    }

    /**
     * 构造服务名
     * @param interfaceName
     * @param version
     * @return
     */
    public static String makeServiceKey(String interfaceName, String version) {
        String serviceKey = interfaceName;
        if (version!=null && version.trim().length()>0) {
            serviceKey += "#".concat(version);
        }
        return serviceKey;
    }


}
