package cn.edu.hubu.rpc.core.remoting.client.netty;


import cn.edu.hubu.rpc.core.remoting.client.ConnectClient;
import cn.edu.hubu.rpc.core.remoting.client.RpcInvokerFactory;
import cn.edu.hubu.rpc.core.remoting.client.netty_http.NettyHttpConnectClient;
import cn.edu.hubu.rpc.core.remoting.codec.NettyDecoder;
import cn.edu.hubu.rpc.core.remoting.codec.NettyEncoder;
import cn.edu.hubu.rpc.core.remoting.message.RpcRequestMessage;
import cn.edu.hubu.rpc.core.remoting.message.RpcResponseMessage;
import cn.edu.hubu.rpc.core.remoting.params.BaseCallback;
import cn.edu.hubu.rpc.core.remoting.params.Beat;
import cn.edu.hubu.rpc.core.serialize.Serializer;
import cn.edu.hubu.rpc.core.utils.IpUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * @Author hxy
 * @Date 2022/4/19
 */

public class NettyConnectClient extends ConnectClient {

    private static NioEventLoopGroup nioEventLoopGroup;

    private Channel channel;

    @Override
    public void init(String address, Serializer serializer, RpcInvokerFactory rpcInvokerFactory) throws Exception {
        // address
        Object[] array = IpUtils.parseIpPort(address);
        String host = (String) array[0];
        int port = (int) array[1];

        // group
        if (nioEventLoopGroup == null) {
            synchronized (NettyHttpConnectClient.class) {
                if (nioEventLoopGroup == null) {
                    nioEventLoopGroup = new NioEventLoopGroup();
                    rpcInvokerFactory.addStopCallBack(new BaseCallback() {
                        @Override
                        public void run() throws Exception {
                            nioEventLoopGroup.shutdownGracefully();
                        }
                    });
                }
            }
        }

        // init
        final NettyConnectClient thisClient = this;
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(nioEventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel channel) throws Exception {
                        channel.pipeline()
                                .addLast(new IdleStateHandler(0,0, Beat.BEAT_INTERVAL, TimeUnit.SECONDS))    // beat N, close if fail
                                .addLast(new NettyEncoder(RpcResponseMessage.class, serializer))
                                .addLast(new NettyDecoder(RpcResponseMessage.class, serializer))
                                .addLast(new NettyClientHandler(rpcInvokerFactory, thisClient));
                    }
                })
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
        this.channel = bootstrap.connect(host, port).sync().channel();

        // valid
        if (!isValidate()) {
            close();
            return;
        }

        logger.debug(">>>>>>>>>>> rpc netty client proxy, connect to server success at host:{}, port:{}", host, port);

    }

    @Override
    public void close() {
        if (this.channel != null && this.channel.isActive()) {
            this.channel.close();        // if this.channel.isOpen()
        }
        logger.debug(">>>>>>>>>>> rpc netty client close.");
    }

    @Override
    public boolean isValidate() {
        if (this.channel != null) {
            return this.channel.isActive();
        }
        return false;
    }

    @Override
    public void send(RpcRequestMessage rpcRequest) throws Exception {
        this.channel.writeAndFlush(rpcRequest).sync();
    }
}
