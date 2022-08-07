package cn.edu.hubu.rpc.core.remoting.client.factory;


import cn.edu.hubu.rpc.core.registry.Register;
import cn.edu.hubu.rpc.core.remoting.client.RpcInvokerFactory;
import cn.edu.hubu.rpc.core.remoting.client.annotation.RpcReference;
import cn.edu.hubu.rpc.core.remoting.client.reference.RpcReferenceBean;
import cn.edu.hubu.rpc.core.remoting.server.RpcProviderFactory;
import cn.edu.hubu.rpc.core.utils.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor;
import org.springframework.util.ReflectionUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @Author hxy
 * @Date 2022/4/7
 *
 * rpc invoker factory, init service-registry and spring-bean by annotation (for spring)
 */

public class RpcSpringInvokerFactory implements SmartInstantiationAwareBeanPostProcessor,InitializingBean, DisposableBean, BeanFactoryAware {
    private Logger logger = LoggerFactory.getLogger(RpcSpringInvokerFactory.class);

    // ---------------------- config ----------------------
    private Class<? extends Register> serviceRegistryClass;          // class.forname
    private Map<String, String> serviceRegistryParam;

    public void setServiceRegistryClass(Class<? extends Register> serviceRegistryClass) {
        this.serviceRegistryClass = serviceRegistryClass;
    }

    public void setServiceRegistryParam(Map<String, String> serviceRegistryParam) {
        this.serviceRegistryParam = serviceRegistryParam;
    }

    // ---------------------- util ----------------------

    private RpcInvokerFactory rpcInvokerFactory;

    @Override
    public void afterPropertiesSet() throws Exception {
        // start invoker factory
        rpcInvokerFactory = new RpcInvokerFactory(serviceRegistryClass, serviceRegistryParam);
        rpcInvokerFactory.start();
    }

    @Override
    public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
        // collection
        final Set<String> serviceKeyList = new HashSet<>();

        // parse RpcReferenceBean
        ReflectionUtils.doWithFields(bean.getClass(),field -> {
            if (field.isAnnotationPresent(RpcReference.class)) {
                //valid
                Class intface = field.getType();
                if (!intface.isInterface()) {
                    throw new RpcException("reference(RpcReference) must be interface.");
                }

                RpcReference rpcReference = field.getAnnotation(RpcReference.class);
                // init reference bean
                RpcReferenceBean referenceBean = new RpcReferenceBean();
                referenceBean.setClient(rpcReference.client());
                referenceBean.setSerializer(rpcReference.serializer());
                referenceBean.setLoadBalance(rpcReference.loadBalance());
                referenceBean.setIface(intface);
                referenceBean.setVersion(rpcReference.version());
                referenceBean.setTimeout(rpcReference.timeout());
                referenceBean.setAddress(rpcReference.address());
                referenceBean.setAccessToken(rpcReference.accessToken());
                referenceBean.setInvokerFactory(rpcInvokerFactory);


                // get proxyObj
                Object serviceProxy = null;
                try {
                    serviceProxy = referenceBean.getObject();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                // set bean
                field.setAccessible(true);
                field.set(bean, serviceProxy);

                logger.info(">>>>>>>>>>> rpc, invoker factory init reference bean success. serviceKey = {}, bean.field = {}.{}",
                        RpcProviderFactory.makeServiceKey(intface.getName(), rpcReference.version()), beanName, field.getName());
                // collection
                String serviceKey = RpcProviderFactory.makeServiceKey(intface.getName(), rpcReference.version());
                serviceKeyList.add(serviceKey);

            }
        });
        // mult discovery
        if (rpcInvokerFactory.getRegister() != null) {
            try {
                rpcInvokerFactory.getRegister().discovery(serviceKeyList);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        return true;
    }

    @Override
    public void destroy() throws Exception {
        // stop invoker factory
        rpcInvokerFactory.stop();
    }


    private BeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }



}
