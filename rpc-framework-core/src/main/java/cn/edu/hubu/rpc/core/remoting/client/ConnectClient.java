package cn.edu.hubu.rpc.core.remoting.client;

import cn.edu.hubu.rpc.core.remoting.client.reference.RpcReferenceBean;
import cn.edu.hubu.rpc.core.remoting.message.RpcRequestMessage;
import cn.edu.hubu.rpc.core.remoting.params.BaseCallback;
import cn.edu.hubu.rpc.core.serialize.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @Author hxy
 * @Date 2022/4/7
 */

public abstract class ConnectClient {

    protected static transient Logger logger = LoggerFactory.getLogger(ConnectClient.class);

    // ---------------------- iface ----------------------

    public abstract void init(String address, final Serializer serializer, final RpcInvokerFactory rpcInvokerFactory) throws Exception;

    public abstract void close();

    public abstract boolean isValidate();

    public abstract void send(RpcRequestMessage rpcRequest) throws Exception;

    /**
     * async send
     */
    public static void asyncSend(RpcRequestMessage rpcRequest, String address,
                                 Class<? extends ConnectClient> connectClientImpl,
                                 final RpcReferenceBean rpcReferenceBean) throws Exception {

        //建立连接 client pool	[tips03 : may save 35ms/100invoke if move it to constructor, but it is necessary. cause by ConcurrentHashMap.get]
        ConnectClient clientPool = ConnectClient.getPool(address, connectClientImpl,rpcReferenceBean);

        try {
            // do invoke
            clientPool.send(rpcRequest);
        } catch (Exception e) {
            throw e;
        }

    }



    private static volatile ConcurrentMap<String, ConnectClient> connectClientMap;        // (static) alread addStopCallBack

    private static volatile ConcurrentMap<String, Object> connectClientLockMap = new ConcurrentHashMap<>();

    private static ConnectClient getPool(String address, Class<? extends ConnectClient> connectClientImpl,RpcReferenceBean rpcReferenceBean) throws Exception {
        // init base connectClientMap, avoid repeat init
        if (connectClientMap == null) {
            synchronized (ConnectClient.class){
                if (connectClientMap == null ){
                    //init
                    connectClientMap = new ConcurrentHashMap<String,ConnectClient>();
                    // stop callback
                    rpcReferenceBean.getInvokerFactory().addStopCallBack(new BaseCallback() {
                        @Override
                        public void run() throws Exception {
                            if (connectClientMap.size() > 0) {
                                for (String key: connectClientMap.keySet()) {
                                    ConnectClient clientPool = connectClientMap.get(key);
                                    clientPool.close();
                                }
                                connectClientMap.clear();
                            }
                        }
                    });
                }
            }
        }
        //get-valid client
        ConnectClient connectClient = connectClientMap.get(address);
        if (connectClient!=null && connectClient.isValidate()) {
            return connectClient;
        }

        //get lock
        Object clientLock = connectClientLockMap.get(address);
        if (clientLock == null){
            connectClientLockMap.putIfAbsent(address,new Object());
            clientLock = connectClientLockMap.get(address);
        }

        //remove-create new client
        synchronized (clientLock){
            //get-valid client ,valid repeat get
            connectClient = connectClientMap.get(address);
            if (connectClient!=null && connectClient.isValidate()) {
                return connectClient;
            }

            //remove old
            if (connectClient !=null) {
                connectClient.close();
                connectClientMap.remove(address);
            }
            //set pool 创建连接
            ConnectClient newConnectClient = connectClientImpl.newInstance();
            try {
                newConnectClient.init(address,rpcReferenceBean.getSerializerInstance(), rpcReferenceBean.getInvokerFactory());
                connectClientMap.put(address, newConnectClient);
            } catch (Exception e) {
                newConnectClient.close();
                throw e;
            }
            return newConnectClient;
        }

    }




}
