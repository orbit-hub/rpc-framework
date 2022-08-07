package cn.edu.hubu.rpc.core.remoting.client.reference;


import cn.edu.hubu.rpc.core.remoting.client.Client;
import cn.edu.hubu.rpc.core.remoting.client.RpcInvokerFactory;
import cn.edu.hubu.rpc.core.remoting.client.call.RpcInvokeCallback;
import cn.edu.hubu.rpc.core.remoting.client.netty_http.NettyHttpClient;
import cn.edu.hubu.rpc.core.remoting.filter.RpcGenericService;
import cn.edu.hubu.rpc.core.remoting.message.RpcFutureResponse;
import cn.edu.hubu.rpc.core.remoting.message.RpcRequestMessage;
import cn.edu.hubu.rpc.core.remoting.message.RpcResponseMessage;
import cn.edu.hubu.rpc.core.remoting.params.CallType;
import cn.edu.hubu.rpc.core.remoting.route.LoadBalance;
import cn.edu.hubu.rpc.core.remoting.server.RpcProviderFactory;
import cn.edu.hubu.rpc.core.serialize.Serializer;
import cn.edu.hubu.rpc.core.serialize.impl.HessianSerializer;
import cn.edu.hubu.rpc.core.utils.ClassUtil;
import cn.edu.hubu.rpc.core.utils.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @Author hxy
 * @Date 2022/4/7
 */

public class RpcReferenceBean {
    private static final Logger logger = LoggerFactory.getLogger(RpcReferenceBean.class);

    // ---------------------- config ----------------------

    private Class<? extends Client> client = NettyHttpClient.class;
    private Class<? extends Serializer> serializer = HessianSerializer.class;
    private CallType callType = CallType.SYNC;
    private LoadBalance loadBalance = LoadBalance.ROUND;

    private Class<?> intface = null;
    private String version = null;

    private long timeout = 10000;

    private String address = null;
    private String accessToken = null;

    private RpcInvokeCallback invokeCallback = null;

    private RpcInvokerFactory invokerFactory = null;

    // set
    public void setClient(Class<? extends Client> client) {
        this.client = client;
    }
    public void setSerializer(Class<? extends Serializer> serializer) {
        this.serializer = serializer;
    }
    public void setCallType(CallType callType) {
        this.callType = callType;
    }
    public void setLoadBalance(LoadBalance loadBalance) {
        this.loadBalance = loadBalance;
    }
    public void setIface(Class<?> intface) {
        this.intface = intface;
    }
    public void setVersion(String version) {
        this.version = version;
    }
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
//    public void setInvokeCallback(RpcInvokeCallback invokeCallback) {
//        this.invokeCallback = invokeCallback;
//    }
    public void setInvokerFactory(RpcInvokerFactory invokerFactory) {
        this.invokerFactory = invokerFactory;
    }


    // get
    public Serializer getSerializerInstance() {
        return serializerInstance;
    }
    public long getTimeout() {
        return timeout;
    }

    public RpcInvokerFactory getInvokerFactory() {
        return invokerFactory;
    }
    public Class<?> getIntface() {
        return intface;
    }

    // ---------------------- initClient ----------------------

    private Client clientInstance = null;
    private Serializer serializerInstance = null;

    public RpcReferenceBean initClient() throws Exception {

        // valid
        if (this.client == null) {
            throw new RpcException("rpc reference client missing.");
        }
        if (this.serializer == null) {
            throw new RpcException("rpc reference serializer missing.");
        }
//        if (this.callType==null) {
//            throw new RpcException("rpc reference callType missing.");
//        }
        if (this.loadBalance==null) {
            throw new RpcException("rpc reference loadBalance missing.");
        }
        if (this.intface==null) {
            throw new RpcException("rpc reference iface missing.");
        }
        if (this.timeout < 0) {
            this.timeout = 0;
        }
        if (this.invokerFactory == null) {
            this.invokerFactory = RpcInvokerFactory.getInstance();
        }

        // init serializerInstance
        this.serializerInstance = serializer.newInstance();

        // init Client
        clientInstance = client.newInstance();
        clientInstance.init(this);

        return this;
    }

    // ---------------------- util ----------------------
    public Object getObject() throws Exception {
        // initClient
        initClient();
        // newProxyInstance
        return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class[]{intface},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        // method param
                        String className = method.getDeclaringClass().getName();	// intface.getName()
                        String varsion_ = version;
                        String methodName = method.getName();
                        Class<?>[] parameterTypes = method.getParameterTypes();
                        Object[] parameters = args;
                        // filter for generic
                        if (className.equals(RpcGenericService.class.getName()) && methodName.equals("invoke")) {

                            Class<?>[] paramTypes = null;
                            if (args[3]!=null) {
                                String[] paramTypes_str = (String[]) args[3];
                                if (paramTypes_str.length > 0) {
                                    paramTypes = new Class[paramTypes_str.length];
                                    for (int i = 0; i < paramTypes_str.length; i++) {
                                        paramTypes[i] = ClassUtil.resolveClass(paramTypes_str[i]);
                                    }
                                }
                            }

                            className = (String) args[0];
                            varsion_ = (String) args[1];
                            methodName = (String) args[2];
                            parameterTypes = paramTypes;
                            parameters = (Object[]) args[4];
                        }
                        // filter method like "Object.toString()"
                        if (className.equals(Object.class.getName())) {
                            logger.info(">>>>>>>>>>> rpc proxy class-method not support [{}#{}]", className, methodName);
                            throw new RpcException("rpc proxy class-method not support");
                        }
                        // address
                        String finalAddress = address;
                        if (finalAddress==null || finalAddress.trim().length()==0) {
                            if (invokerFactory!=null && invokerFactory.getRegister()!=null) {
                                // discovery
                                String serviceKey = RpcProviderFactory.makeServiceKey(className, varsion_);
                                TreeSet<String> addressSet = invokerFactory.getRegister().discovery(serviceKey);
                                // load balance
                                if (addressSet == null || addressSet.size()==0){

                                }else if (addressSet.size()==1){
                                    finalAddress = addressSet.first();
                                }else {
                                    finalAddress = loadBalance.rpcInvokerRouter.route(serviceKey,addressSet);
                                }

                            }
                        }
                        if (finalAddress==null || finalAddress.trim().length()==0) {
                            throw new RpcException("rpc reference bean["+ className +"] address empty");
                        }

                        // request
                        RpcRequestMessage rpcRequest = new RpcRequestMessage();
                        rpcRequest.setRequestId(UUID.randomUUID().toString());
                        rpcRequest.setCreateMillisTime(System.currentTimeMillis());
                        rpcRequest.setMethodName(methodName);
                        rpcRequest.setAccessToken(accessToken);
                        rpcRequest.setInterfaceName(className);
                        rpcRequest.setParameterTypes(parameterTypes);
                        rpcRequest.setParameterValue(parameters);
                        rpcRequest.setVersion(version);
                        //send
                        // future-response set
                        RpcFutureResponse futureResponse = new RpcFutureResponse(invokerFactory, rpcRequest, null);
                        try {
                            // do invoke
                            clientInstance.asyncSend(finalAddress, rpcRequest);

                            // future get
                            RpcResponseMessage rpcResponse = futureResponse.get(timeout, TimeUnit.MILLISECONDS);
                            if (rpcResponse.getErrorMsg() != null) {
                                throw new RpcException(rpcResponse.getErrorMsg());
                            }
                            return rpcResponse.getData();
                        } catch (Exception e) {
                            logger.info(">>>>>>>>>>> rpc, invoke error, address:{}, RpcRequest{}", finalAddress, rpcRequest);

                            throw (e instanceof RpcException)?e:new RpcException(e);
                        } finally{
                            // future-response remove
                            futureResponse.removeInvokerFuture();
                        }
                    }
                });
    }

}
