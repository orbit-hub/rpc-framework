package cn.edu.hubu.server.service;

import cn.edu.hubu.HelloService;
import cn.edu.hubu.dto.Student;
import cn.edu.hubu.rpc.core.remoting.server.annotation.RpcService;
import org.springframework.stereotype.Service;

/**
 * @Author hxy
 * @Date 2022/4/6
 */
@RpcService
@Service
public class HelloServiceImpl implements HelloService {
    @Override
    public Student sayHello(String str) {
        return new Student(11, str);
    }
}
