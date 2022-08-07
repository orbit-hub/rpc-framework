package cn.edu.hubu.rpc.core.remoting.server.netty;


import cn.edu.hubu.rpc.core.remoting.message.RpcRequestMessage;
import cn.edu.hubu.rpc.core.remoting.message.RpcResponseMessage;
import cn.edu.hubu.rpc.core.remoting.params.Beat;
import cn.edu.hubu.rpc.core.remoting.server.RpcProviderFactory;
import cn.edu.hubu.rpc.core.utils.ThrowableUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Author hxy
 * @Date 2022/4/19
 */

public class NettyServerHandler extends SimpleChannelInboundHandler<RpcRequestMessage> {
    private static final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);

    private RpcProviderFactory rpcProviderFactory;
    private ThreadPoolExecutor serverHandlerPool;

    public NettyServerHandler(final RpcProviderFactory rpcProviderFactory, final ThreadPoolExecutor serverHandlerPool) {
        this.rpcProviderFactory = rpcProviderFactory;
        this.serverHandlerPool = serverHandlerPool;
    }
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcRequestMessage rpcRequestMessage) throws Exception {
        // filter beat
        if (Beat.BEAT_ID.equalsIgnoreCase(rpcRequestMessage.getRequestId())){
            logger.debug(">>>>>>>>>>> rpc provider netty server read beat-ping.");
            return;
        }
        try {
            serverHandlerPool.execute(()->{
                RpcResponseMessage responseMessage = rpcProviderFactory.invokeService(rpcRequestMessage);
                channelHandlerContext.writeAndFlush(responseMessage);
            });
        }catch (Exception e){
            // catch error
            RpcResponseMessage responseMessage = new RpcResponseMessage();
            responseMessage.setRequestId(responseMessage.getRequestId());
            responseMessage.setErrorMsg(ThrowableUtil.toString(e));

            channelHandlerContext.writeAndFlush(responseMessage);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error(">>>>>>>>>>> rpc provider netty server caught exception", cause);
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent){
            ctx.channel().close();      // beat 3N, close if idle
            logger.debug(">>>>>>>>>>> rpc provider netty server close an idle channel.");
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
