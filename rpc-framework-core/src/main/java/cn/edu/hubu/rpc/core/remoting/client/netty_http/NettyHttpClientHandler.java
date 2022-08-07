package cn.edu.hubu.rpc.core.remoting.client.netty_http;


import cn.edu.hubu.rpc.core.remoting.client.RpcInvokerFactory;
import cn.edu.hubu.rpc.core.remoting.message.RpcResponseMessage;
import cn.edu.hubu.rpc.core.remoting.params.Beat;
import cn.edu.hubu.rpc.core.serialize.Serializer;
import cn.edu.hubu.rpc.core.utils.RpcException;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author hxy
 * @Date 2022/4/11
 *
 * netty_http
 */

public class NettyHttpClientHandler extends SimpleChannelInboundHandler<FullHttpResponse> {
    private static final Logger logger = LoggerFactory.getLogger(NettyHttpClientHandler.class);

    private RpcInvokerFactory rpcInvokerFactory;
    private Serializer serializer;
    private NettyHttpConnectClient nettyHttpConnectClient;

    public NettyHttpClientHandler(final RpcInvokerFactory rpcInvokerFactory, Serializer serializer, final NettyHttpConnectClient nettyHttpConnectClient) {
        this.rpcInvokerFactory = rpcInvokerFactory;
        this.serializer = serializer;
        this.nettyHttpConnectClient = nettyHttpConnectClient;
    }


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpResponse response) throws Exception {
        //valid status
        if (!HttpResponseStatus.OK.equals(response.status())) {
           throw new RpcException("rpc response status invalid.");
        }

        //response parse
        byte[] responseBytes = ByteBufUtil.getBytes(response.content());
        //valid length
        if (responseBytes.length == 0) {
            throw new RpcException("rpc response data empty.");
        }
        // response deserialize
        RpcResponseMessage rpcResponseMessage = serializer.deserialize(RpcResponseMessage.class, responseBytes);
        // notify response
        rpcInvokerFactory.notifyInvokerFuture(rpcResponseMessage.getRequestId(), rpcResponseMessage);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(">>>>>>>>>>> rpc netty_http client caught exception", cause);
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
       if (evt instanceof IdleStateEvent){

            nettyHttpConnectClient.send(Beat.BEAT_PING);    // beat N, close if fail(may throw error)
            logger.debug(">>>>>>>>>>> rpc netty_http client send beat-ping.");
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
