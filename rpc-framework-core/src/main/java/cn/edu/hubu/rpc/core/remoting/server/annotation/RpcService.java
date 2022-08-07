package cn.edu.hubu.rpc.core.remoting.server.annotation;

import java.lang.annotation.*;

/**
 * @Author hxy
 * @Date 2022/4/12
 *
 *  rpc service annotation, skeleton of stub ("@Inherited" allow service use "Transactional")
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface RpcService {



    /**
     * @return
     */
    String version() default "";
}
