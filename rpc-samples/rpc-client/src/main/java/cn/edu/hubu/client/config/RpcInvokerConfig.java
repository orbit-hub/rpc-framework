package cn.edu.hubu.client.config;

import cn.edu.hubu.rpc.core.registry.nacos.NacosRegistry;
import cn.edu.hubu.rpc.core.remoting.client.factory.RpcSpringInvokerFactory;
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
public class RpcInvokerConfig {
    private Logger logger = LoggerFactory.getLogger(RpcInvokerConfig.class);
    @Value("${rpc.discovery.server-addr}")
    private String address;
    @Bean
    public RpcSpringInvokerFactory jobExecutor() {

        RpcSpringInvokerFactory invokerFactory = new RpcSpringInvokerFactory();
        invokerFactory.setServiceRegistryClass(NacosRegistry.class);
        invokerFactory.setServiceRegistryParam(new HashMap<String, String>(){{
            put("serverAddr", address);
//            put(RpcRegister.ENV, env);
        }});

        logger.info(">>>>>>>>>>> rpc invoker config init finish.");
        return invokerFactory;
    }
}
