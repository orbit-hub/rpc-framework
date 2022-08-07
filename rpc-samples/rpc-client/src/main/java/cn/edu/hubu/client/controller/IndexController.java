package cn.edu.hubu.client.controller;

import cn.edu.hubu.EchoService;
import cn.edu.hubu.HelloService;
import cn.edu.hubu.dto.Student;
import cn.edu.hubu.rpc.core.remoting.client.annotation.RpcReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Author hxy
 * @Date 2022/4/6
 */
@Controller
public class IndexController {

    @RpcReference
    private HelloService helloService;
    @RpcReference
    private EchoService echoService;
    @RequestMapping("")
    @ResponseBody
    public Student http(String name) {
        try {
            return helloService.sayHello(name);
        } catch (Exception e) {
            e.printStackTrace();
            return new Student(Integer.MAX_VALUE, e.getMessage());
        }
    }
    @RequestMapping("/echo")
    @ResponseBody
    public String echoClient(String req) {
        try {
            return echoService.echo(req);
        } catch (Exception e) {
            e.printStackTrace();
            return req;
        }
    }


}
