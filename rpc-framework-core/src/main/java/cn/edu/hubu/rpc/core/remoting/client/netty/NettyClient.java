package cn.edu.hubu.rpc.core.remoting.client.netty;

import cn.edu.hubu.rpc.core.remoting.client.Client;
import cn.edu.hubu.rpc.core.remoting.client.ConnectClient;
import cn.edu.hubu.rpc.core.remoting.message.RpcRequestMessage;

/**
 * @Author hxy
 * @Date 2022/4/19
 */

public class NettyClient extends Client {
    private Class<? extends ConnectClient> connectClientImpl = NettyConnectClient.class;

    @Override
    public void asyncSend(String address, RpcRequestMessage rpcRequest) throws Exception {
        ConnectClient.asyncSend(rpcRequest, address, connectClientImpl, rpcReferenceBean);
    }
}
