package cn.edu.hubu.rpc.core.remoting.server.impl;


import cn.edu.hubu.rpc.core.remoting.server.RpcProviderFactory;
import cn.edu.hubu.rpc.core.remoting.server.annotation.RpcService;
import cn.edu.hubu.rpc.core.utils.RpcException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

/**
 * @Author hxy
 * @Date 2022/4/13
 *
 *  xxl-rpc provider (for spring)
 */

public class RpcSpringProviderFactory extends RpcProviderFactory implements ApplicationContextAware, InitializingBean, DisposableBean {


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(RpcService.class);
        if (serviceBeanMap!=null && serviceBeanMap.size()>0) {
            for (Object serviceBean : serviceBeanMap.values()) {
                //valid
                if (serviceBean.getClass().getInterfaces().length == 0) {
                    throw new RpcException("rpc, service(RpcService) must inherit interface.");
                }
                //add service
                RpcService rpcService = serviceBean.getClass().getAnnotation(RpcService.class);
                String intface = serviceBean.getClass().getInterfaces()[0].getName();
                String version = rpcService.version();
                super.addService(intface,version,serviceBean);
            }
        }

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.start();
    }


    @Override
    public void destroy() throws Exception {
        super.stop();
    }
}
