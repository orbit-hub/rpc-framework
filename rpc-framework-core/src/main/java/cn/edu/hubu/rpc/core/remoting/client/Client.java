package cn.edu.hubu.rpc.core.remoting.client;

import cn.edu.hubu.rpc.core.remoting.client.reference.RpcReferenceBean;
import cn.edu.hubu.rpc.core.remoting.message.RpcRequestMessage;

/**
 * @Author hxy
 * @Date 2022/4/7
 */

public abstract class Client {

    // ---------------------- init ----------------------

    protected volatile RpcReferenceBean rpcReferenceBean;

    public void init(RpcReferenceBean rpcReferenceBean) {
        this.rpcReferenceBean = rpcReferenceBean;
    }


    // ---------------------- send ----------------------

    /**
     * async send, bind requestId and future-response
     *
     * @param address
     * @param rpcRequest
     * @return
     * @throws Exception
     */
    public abstract void asyncSend(String address, RpcRequestMessage rpcRequest) throws Exception;


}
