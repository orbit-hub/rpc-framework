package cn.edu.hubu.rpc.core.remoting.server.netty_http;


import cn.edu.hubu.rpc.core.remoting.message.RpcRequestMessage;
import cn.edu.hubu.rpc.core.remoting.message.RpcResponseMessage;
import cn.edu.hubu.rpc.core.remoting.params.Beat;
import cn.edu.hubu.rpc.core.remoting.server.RpcProviderFactory;
import cn.edu.hubu.rpc.core.utils.RpcException;
import cn.edu.hubu.rpc.core.utils.ThrowableUtil;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Author hxy
 * @Date 2022/4/11
 */

public class NettyHttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final Logger logger = LoggerFactory.getLogger(NettyHttpServerHandler.class);

    private RpcProviderFactory rpcProviderFactory;
    private ThreadPoolExecutor serverHandlerPool;

    public NettyHttpServerHandler(final RpcProviderFactory rpcProviderFactory, final ThreadPoolExecutor serverHandlerPool) {
        this.rpcProviderFactory = rpcProviderFactory;
        this.serverHandlerPool = serverHandlerPool;
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        //解析请求
        byte[] requestBytes = ByteBufUtil.getBytes(msg.content());
        String uri = msg.uri();
        boolean keepAlive = HttpUtil.isKeepAlive(msg);

        // 异步调用
        serverHandlerPool.execute(()-> {
            process(ctx, uri, requestBytes, keepAlive);
        });
    }

    /**
     * 处理请求
     * @param ctx
     * @param uri
     * @param requestBytes
     * @param keepAlive
     */
    private void process(ChannelHandlerContext ctx, String uri, byte[] requestBytes, boolean keepAlive) {
        String requestId = null;
        try {
            if ("/services".equals(uri)) {  //services mapping
                // request
                StringBuffer stringBuffer = new StringBuffer("<ui>");
                for (String serviceKey : rpcProviderFactory.getServiceData().keySet()) {
                    stringBuffer.append("<li>")
                            .append(serviceKey)
                            .append(": ")
                            .append(rpcProviderFactory.getServiceData().get(serviceKey))
                            .append("</li>");
                }
                stringBuffer.append("</ui>");

                //response serialize
                byte[] responseBytes = stringBuffer.toString().getBytes(StandardCharsets.UTF_8);

                // response-write
                writeResponse(ctx, keepAlive, responseBytes);
            }else {
                // valid
                if (requestBytes.length == 0) {
                    throw new RpcException("rpc request data empty.");
                }
                // request deserialize
                RpcRequestMessage rpcRequestMessage = rpcProviderFactory.getSerializerInstance().deserialize(RpcRequestMessage.class,requestBytes);
                requestId = rpcRequestMessage.getRequestId();

                // filter beat
                if (Beat.BEAT_ID.equalsIgnoreCase(rpcRequestMessage.getRequestId())){
                    logger.debug(">>>>>>>>>>> rpc provider netty_http server read beat-ping.");
                    return;
                }

                // invoke + response  远程服务调用获取返回值
                RpcResponseMessage rpcResponseMessage = rpcProviderFactory.invokeService(rpcRequestMessage);

                //response serialize
                byte[] responseBytes = rpcProviderFactory.getSerializerInstance().serialize(rpcResponseMessage);
                // response-write
                writeResponse(ctx, keepAlive, responseBytes);
            }
        }catch (Exception e) {
            logger.error(e.getMessage(), e);

            // response error
            RpcResponseMessage rpcResponseMessage = new RpcResponseMessage();
            rpcResponseMessage.setRequestId(requestId);
            rpcResponseMessage.setErrorMsg(ThrowableUtil.toString(e));

            // response serialize
            byte[] responseBytes = rpcProviderFactory.getSerializerInstance().serialize(rpcResponseMessage);

            // response-write
            writeResponse(ctx, keepAlive, responseBytes);
        }

    }

    /**
     *  write response
     * @param ctx
     * @param keepAlive
     * @param responseBytes
     */
    private void writeResponse(ChannelHandlerContext ctx, boolean keepAlive, byte[] responseBytes) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(responseBytes));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html;charset=UTF-8");       // HttpHeaderValues.TEXT_PLAIN.toString()
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        if (keepAlive) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }
        ctx.writeAndFlush(response);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent){
            ctx.channel().close();      // beat 3N, close if idle
            logger.debug(">>>>>>>>>>> rpc provider netty_http server close an idle channel.");
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(">>>>>>>>>>> rpc provider netty_http server caught exception", cause);
        ctx.close();
    }
}
