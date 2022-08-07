package cn.edu.hubu.rpc.core.remoting.codec;


import cn.edu.hubu.rpc.core.remoting.message.RpcConstants;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * RPC解码器
 *
 * @Author hxy
 * @Date 2022/4/21
 */

public class RpcDecoder extends ByteToMessageDecoder {
    /**
     * 协议的开头部分的标准长度
     */
    public final int BASE_LENGTH = 2 + 4;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out)  {
        if (byteBuf.readableBytes() >= BASE_LENGTH) {
            if (!(byteBuf.readShort() == RpcConstants.MAGIC_NUMBER)) {
                ctx.close();
                return;
            }
            int length = byteBuf.readInt();
            if (byteBuf.readableBytes() < length) {
                //数据包有异常
                ctx.close();
                return;
            }
            byte[] body = new byte[length];
            byteBuf.readBytes(body);
            RpcProtocol rpcProtocol = new RpcProtocol(body);
            out.add(rpcProtocol);
        }
    }
}
