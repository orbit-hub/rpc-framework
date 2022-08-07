package cn.edu.hubu.rpc.core.remoting.server.netty;


import cn.edu.hubu.rpc.core.remoting.codec.NettyDecoder;
import cn.edu.hubu.rpc.core.remoting.codec.NettyEncoder;
import cn.edu.hubu.rpc.core.remoting.message.RpcRequestMessage;
import cn.edu.hubu.rpc.core.remoting.message.RpcResponseMessage;
import cn.edu.hubu.rpc.core.remoting.params.Beat;
import cn.edu.hubu.rpc.core.remoting.server.RpcProviderFactory;
import cn.edu.hubu.rpc.core.remoting.server.Server;
import cn.edu.hubu.rpc.core.utils.ThreadPoolUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author hxy
 * @Date 2022/4/14
 *
 * netty rpc server
 */

public class NettyServer extends Server {

    private Thread thread;

    @Override
    public void start(RpcProviderFactory rpcProviderFactory) throws Exception {
        thread = new Thread(()-> {
            //param
            final ThreadPoolExecutor serverHandlerPool = ThreadPoolUtil.makeServerThreadPool(
                    NettyServer.class.getSimpleName(),
                    rpcProviderFactory.getCorePoolSize(),
                    rpcProviderFactory.getMaxPoolSize());
            EventLoopGroup bossGroup = new NioEventLoopGroup();
            EventLoopGroup workerGroup = new NioEventLoopGroup();

            try {
                // start server
                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            public void initChannel(SocketChannel channel) throws Exception {
                                channel.pipeline()
                                        .addLast(new IdleStateHandler(0,0, Beat.BEAT_INTERVAL*3, TimeUnit.SECONDS))     // beat 3N, close if idle
                                        .addLast(new NettyDecoder(RpcRequestMessage.class, rpcProviderFactory.getSerializerInstance()))
                                        .addLast(new NettyEncoder(RpcResponseMessage.class, rpcProviderFactory.getSerializerInstance()))
                                        .addLast(new NettyServerHandler(rpcProviderFactory, serverHandlerPool));
                            }
                        })
                        .childOption(ChannelOption.TCP_NODELAY, true)
                        .childOption(ChannelOption.SO_KEEPALIVE, true);


                // bind
                ChannelFuture future = bootstrap.bind(rpcProviderFactory.getPort()).sync();

                logger.info(">>>>>>>>>>> rpc remoting server start success, nettype = {}, port = {}", NettyServer.class.getName(), rpcProviderFactory.getPort());
                onStarted();

                // wait util stop
                future.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }finally {
                // stop
                try {
                    serverHandlerPool.shutdown();    // shutdownNow
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                try {
                    workerGroup.shutdownGracefully();
                    bossGroup.shutdownGracefully();
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void stop() throws Exception {

        // destroy server thread
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        }

        // on stop
        onStoped();
        logger.info(">>>>>>>>>>> rpc remoting server destroy success.");
    }
}
