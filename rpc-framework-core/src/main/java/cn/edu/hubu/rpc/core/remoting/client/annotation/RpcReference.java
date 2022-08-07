package cn.edu.hubu.rpc.core.remoting.client.annotation;

import cn.edu.hubu.rpc.core.remoting.client.Client;
import cn.edu.hubu.rpc.core.remoting.client.netty_http.NettyHttpClient;
import cn.edu.hubu.rpc.core.remoting.route.LoadBalance;
import cn.edu.hubu.rpc.core.serialize.Serializer;
import cn.edu.hubu.rpc.core.serialize.impl.HessianSerializer;

import java.lang.annotation.*;

/**
 * @Author hxy
 * @Date 2022/4/7
 */

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface RpcReference {

    Class<? extends Client> client() default NettyHttpClient.class;
    Class<? extends Serializer> serializer() default HessianSerializer.class;

    LoadBalance loadBalance() default LoadBalance.ROUND;

    String version() default "";

    long timeout() default 1000;

    String address() default "";

    String accessToken() default "";

//    String register() default "nacos";


}
