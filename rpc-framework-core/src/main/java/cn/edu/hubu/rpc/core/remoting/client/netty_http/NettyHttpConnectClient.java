package cn.edu.hubu.rpc.core.remoting.client.netty_http;

import cn.edu.hubu.rpc.core.remoting.client.ConnectClient;
import cn.edu.hubu.rpc.core.remoting.client.RpcInvokerFactory;
import cn.edu.hubu.rpc.core.remoting.message.RpcRequestMessage;
import cn.edu.hubu.rpc.core.remoting.params.BaseCallback;
import cn.edu.hubu.rpc.core.remoting.params.Beat;
import cn.edu.hubu.rpc.core.serialize.Serializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.timeout.IdleStateHandler;

import java.net.URI;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * @Author hxy
 * @Date 2022/4/7
 */

public class NettyHttpConnectClient extends ConnectClient {

    private static NioEventLoopGroup nioEventLoopGroup;

    private Channel channel;

    private Serializer serializer;
    private String address;
    private String host;


    @Override
    public void init(String address, Serializer serializer, RpcInvokerFactory rpcInvokerFactory) throws Exception {
        // address
        if (!address.toLowerCase().startsWith("http")){
            address = "http://"+address;
        }

        this.address = address;
        URL url = new URL(address);
        this.host = url.getHost();
        int port = url.getPort()>-1?url.getPort():80;

        //group
        if (nioEventLoopGroup == null){
            synchronized (NettyHttpConnectClient.class){
                if (nioEventLoopGroup == null){
                    nioEventLoopGroup = new NioEventLoopGroup();
//                    rpcInvokerFactory.addstopcallback TODO
                    rpcInvokerFactory.addStopCallBack(new BaseCallback() {
                        @Override
                        public void run() throws Exception {
                            nioEventLoopGroup.shutdownGracefully();
                        }
                    });
                }
            }
        }

        //init
        final NettyHttpConnectClient thisClient = this;
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new IdleStateHandler(0, 0, Beat.BEAT_INTERVAL, TimeUnit.SECONDS))
                                .addLast(new HttpClientCodec())
                                .addLast(new HttpObjectAggregator(5 * 1024 * 1024))
                                .addLast(new NettyHttpClientHandler(rpcInvokerFactory, serializer, thisClient));
                    }
                })
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
        this.channel = bootstrap.connect(host,port).sync().channel();

        this.serializer = serializer;
        // valid
        if (!isValidate()) {
            close();
            return;
        }
        logger.debug(">>>>>>>>>>> rpc netty client proxy, connect to server success at host:{}, port:{}", host, port);
    }

    @Override
    public void close() {
        if (this.channel!=null && this.channel.isActive()) {
            this.channel.close();		// if this.channel.isOpen()
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
        byte[] bytes = serializer.serialize(rpcRequest);
        DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
                HttpMethod.POST,
                new URI(address).getRawPath(),
                Unpooled.wrappedBuffer(bytes));
        request.headers().set(HttpHeaderNames.HOST, host);
        request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        request.headers().set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());
        this.channel.writeAndFlush(request).sync();

    }
}
