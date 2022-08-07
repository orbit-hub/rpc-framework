package cn.edu.hubu.rpc.core.remoting.client.netty;

import cn.edu.hubu.rpc.core.remoting.client.RpcInvokerFactory;
import cn.edu.hubu.rpc.core.remoting.message.RpcResponseMessage;
import cn.edu.hubu.rpc.core.remoting.params.Beat;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author hxy
 * @Date 2022/4/19
 */

public class NettyClientHandler extends SimpleChannelInboundHandler<RpcResponseMessage> {
    private static final Logger logger = LoggerFactory.getLogger(NettyClientHandler.class);


    private RpcInvokerFactory rpcInvokerFactory;
    private NettyConnectClient nettyConnectClient;
    public NettyClientHandler(final RpcInvokerFactory rpcInvokerFactory, NettyConnectClient nettyConnectClient) {
        this.rpcInvokerFactory = rpcInvokerFactory;
        this.nettyConnectClient = nettyConnectClient;
    }
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponseMessage rpcResponseMessage) throws Exception {

        // notify response
        rpcInvokerFactory.notifyInvokerFuture(rpcResponseMessage.getRequestId(), rpcResponseMessage);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(">>>>>>>>>>> rpc netty client caught exception", cause);
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent){

            nettyConnectClient.send(Beat.BEAT_PING);	// beat N, close if fail(may throw error)
            logger.debug(">>>>>>>>>>> rpc netty client send beat-ping.");

        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
