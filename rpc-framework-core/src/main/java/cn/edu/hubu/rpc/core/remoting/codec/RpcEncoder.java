package cn.edu.hubu.rpc.core.remoting.codec;


import cn.edu.hubu.rpc.core.remoting.message.RpcConstants;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 *
 * RPC编码器
 * @Author hxy
 * @Date 2022/4/21
 */

public class RpcEncoder extends MessageToByteEncoder<RpcProtocol> {

    @Override
    protected void encode(ChannelHandlerContext ctx, RpcProtocol msg, ByteBuf out) throws Exception {
        out.writeShort(msg.getMagicNumber());
        out.writeInt(msg.getContentLength());
        out.writeBytes(msg.getContent());
        out.writeBytes(RpcConstants.DEFAULT_DECODE_CHAR.getBytes());
    }
}
