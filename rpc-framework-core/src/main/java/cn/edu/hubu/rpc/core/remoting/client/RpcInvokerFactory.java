package cn.edu.hubu.rpc.core.remoting.client;


import cn.edu.hubu.rpc.core.extension.ExtensionLoader;
import cn.edu.hubu.rpc.core.registry.Register;
import cn.edu.hubu.rpc.core.remoting.message.RpcFutureResponse;
import cn.edu.hubu.rpc.core.remoting.message.RpcResponseMessage;
import cn.edu.hubu.rpc.core.remoting.params.BaseCallback;
import cn.edu.hubu.rpc.core.utils.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @Author hxy
 * @Date 2022/4/7
 *
 * invoker factory, init service-registry
 */

public class RpcInvokerFactory {

    private static Logger logger = LoggerFactory.getLogger(RpcInvokerFactory.class);

    // ---------------------- default instance ----------------------

    private static volatile RpcInvokerFactory instance = new RpcInvokerFactory(Register.class, null);


    public static RpcInvokerFactory getInstance() {
        return instance;
    }

    // ---------------------- config ----------------------

    private Class<? extends Register> serviceRegistryClass;          // class.forname
    private Map<String, String> serviceRegistryParam;
//
//
    public RpcInvokerFactory() {
    }
    public RpcInvokerFactory(Class<? extends Register> serviceRegistryClass, Map<String, String> serviceRegistryParam) {
        this.serviceRegistryClass = serviceRegistryClass;
        this.serviceRegistryParam = serviceRegistryParam;
    }


    // ---------------------- start / stop ----------------------

    public void start() throws Exception {
        // start registry
        if (serviceRegistryClass != null) {
//            register = serviceRegistryClass.newInstance();
            register = ExtensionLoader
                    .getExtensionLoader(Register.class)
                    .getExtension("zk");
            register.start(serviceRegistryParam);
        }
    }

    public void  stop() throws Exception {
        // stop registry

    }
    // ---------------------- service registry ----------------------
    private Register register;
    public Register getRegister() {
        return register;
    }
    // ---------------------- service registry ----------------------

    private List<BaseCallback> stopCallbackList = new ArrayList<BaseCallback>();

    public void addStopCallBack(BaseCallback callback){
        stopCallbackList.add(callback);
    }

    // ---------------------- future-response pool ----------------------
    //RpcFutureResponseFactory

    private ConcurrentMap<String, RpcFutureResponse> futureResponsePool = new ConcurrentHashMap<String, RpcFutureResponse>();
    public void setInvokerFuture(String requestId, RpcFutureResponse futureResponse){
        futureResponsePool.put(requestId, futureResponse);
    }
    public void removeInvokerFuture(String requestId){
        futureResponsePool.remove(requestId);
    }
    public void notifyInvokerFuture(String requestId, final RpcResponseMessage rpcResponse){

        // get
        final RpcFutureResponse futureResponse = futureResponsePool.get(requestId);
        if (futureResponse == null) {
            return;
        }

        // notify
        if (futureResponse.getInvokeCallback()!=null) {

            // callback type
            try {
                executeResponseCallback(new Runnable() {
                    @Override
                    public void run() {
                        if (rpcResponse.getErrorMsg() != null) {
                            futureResponse.getInvokeCallback().onFailure(new RpcException(rpcResponse.getErrorMsg()));
                        } else {
                            futureResponse.getInvokeCallback().onSuccess(rpcResponse.getData());
                        }
                    }
                });
            }catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        } else {

            // other nomal type
            futureResponse.setResponse(rpcResponse);
        }

        // do remove
        futureResponsePool.remove(requestId);

    }

    // ---------------------- response callback ThreadPool ----------------------

    private ThreadPoolExecutor responseCallbackThreadPool = null;
    public void executeResponseCallback(Runnable runnable){

        if (responseCallbackThreadPool == null) {
            synchronized (this) {
                if (responseCallbackThreadPool == null) {
                    responseCallbackThreadPool = new ThreadPoolExecutor(
                            10,
                            100,
                            60L,
                            TimeUnit.SECONDS,
                            new LinkedBlockingQueue<Runnable>(1000),
                            new ThreadFactory() {
                                @Override
                                public Thread newThread(Runnable r) {
                                    return new Thread(r, "rpc, RpcInvokerFactory-responseCallbackThreadPool-" + r.hashCode());
                                }
                            },
                            new RejectedExecutionHandler() {
                                @Override
                                public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                                    throw new RpcException("rpc Invoke Callback Thread pool is EXHAUSTED!");
                                }
                            });		// default maxThreads 300, minThreads 60
                }
            }
        }
        responseCallbackThreadPool.execute(runnable);
    }
    public void stopCallbackThreadPool() {
        if (responseCallbackThreadPool != null) {
            responseCallbackThreadPool.shutdown();
        }
    }

}
