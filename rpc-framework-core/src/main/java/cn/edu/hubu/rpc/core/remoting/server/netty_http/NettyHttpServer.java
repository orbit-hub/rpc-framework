package cn.edu.hubu.rpc.core.remoting.server.netty_http;


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
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author hxy
 * @Date 2022/4/11
 *
 * netty_http_server
 */

public class NettyHttpServer extends Server {
    private Thread thread;


    @Override
    public void start(RpcProviderFactory rpcProviderFactory) {
        thread = new Thread(()-> {
            // param
            final ThreadPoolExecutor serverHandlerPool = ThreadPoolUtil.makeServerThreadPool(
                    NettyHttpServer.class.getSimpleName(),
                    rpcProviderFactory.getCorePoolSize(),
                    rpcProviderFactory.getMaxPoolSize());
            EventLoopGroup bossGroup = new NioEventLoopGroup();
            EventLoopGroup workerGroup = new NioEventLoopGroup();

            try {
                //start server
                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            public void initChannel(SocketChannel channel) throws Exception {
                                channel.pipeline()
                                        .addLast(new IdleStateHandler(0, 0, Beat.BEAT_INTERVAL * 3, TimeUnit.SECONDS))  // beat 3N, close if idle
                                        .addLast(new HttpServerCodec())
                                        .addLast(new HttpObjectAggregator(5 * 1024 * 1024))  // merge request & reponse to FULL
                                        .addLast(new NettyHttpServerHandler(rpcProviderFactory, serverHandlerPool));
                            }
                        });
                //有数据立即发送
                bootstrap.option(ChannelOption.TCP_NODELAY, true);
                //保持连接数
                bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
                bootstrap.option(ChannelOption.SO_SNDBUF, 16 * 1024)
                        .option(ChannelOption.SO_RCVBUF, 16 * 1024)
                        //长链接
                        .option(ChannelOption.SO_KEEPALIVE, true);
                // bind
                ChannelFuture future = bootstrap.bind(rpcProviderFactory.getPort()).sync();
                logger.info(">>>>>>>>>>> rpc remoting server start success, nettype = {}, port = {}", NettyHttpServer.class.getName(), rpcProviderFactory.getPort());
                onStarted(); //服务器开启时，注册服务

                // wait util stop
                future.channel().closeFuture().sync();
            }catch (InterruptedException e) {
                if (e instanceof InterruptedException) {
                    logger.info(">>>>>>>>>>> rpc remoting server stop.");
                } else {
                    logger.error(">>>>>>>>>>> rpc remoting server error.", e);
                }
            } finally {
                try {
                    serverHandlerPool.shutdown();	// 关闭线程池
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
        thread.setDaemon(true);	// daemon
        thread.start();
    }


    @Override
    public void stop() throws Exception {
        // destroy server thread
        if (thread!=null && thread.isAlive()) {
            thread.interrupt();
        }
        //netty服务关闭时移除在注册的服务
        onStoped();
        logger.info(">>>>>>>>>>> rpc remoting server destroy success.");
    }
}
