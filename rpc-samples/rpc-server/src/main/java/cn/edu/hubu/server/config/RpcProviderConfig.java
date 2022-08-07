package cn.edu.hubu.server.config;

import cn.edu.hubu.rpc.core.registry.nacos.NacosRegistry;
import cn.edu.hubu.rpc.core.remoting.server.impl.RpcSpringProviderFactory;
import cn.edu.hubu.rpc.core.remoting.server.netty_http.NettyHttpServer;
import cn.edu.hubu.rpc.core.serialize.impl.HessianSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

/**
 * @Author hxy
 * @Date 2022/4/13
 */
@Configuration
public class RpcProviderConfig {
    private Logger logger = LoggerFactory.getLogger(RpcProviderConfig.class);
    @Value("${rpc.remoting.port}")
    private int port;

    @Value("${rpc.registry.address}")
    private String address;
//
//    @Value("${rpc.registry.zk.address}")
//    private String namespace;
    @Bean
    public RpcSpringProviderFactory RpcSpringProviderFactory() {
        RpcSpringProviderFactory providerFactory = new RpcSpringProviderFactory();
        providerFactory.setServer(NettyHttpServer.class);
        providerFactory.setSerializer(HessianSerializer.class);
        providerFactory.setCorePoolSize(-1);
        providerFactory.setMaxPoolSize(-1);
        providerFactory.setIp(null);
        providerFactory.setPort(port);
        providerFactory.setAccessToken(null);

        providerFactory.setServiceRegistry(NacosRegistry.class);
        providerFactory.setServiceRegistryParam(new HashMap<String, String>() {{
            put("serverAddr", address);
//            put("namespace", env);
        }});

        logger.info(">>>>>>>>>>> rpc provider config init finish.");
        return providerFactory;
    }

}
