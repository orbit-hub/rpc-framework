package cn.edu.hubu.server.service;


import cn.edu.hubu.EchoService;
import cn.edu.hubu.rpc.core.remoting.server.annotation.RpcService;
import org.springframework.stereotype.Service;

/**
 * @Author hxy
 * @Date 2022/4/18
 */
@RpcService
@Service
public class EchoServiceImpl implements EchoService {
    @Override
    public String echo(String req) {
        return req+"===这是server提供者实现的echo服务";
    }
}
