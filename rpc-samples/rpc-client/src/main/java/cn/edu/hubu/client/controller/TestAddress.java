//package cn.edu.hubu.client.controller;
//
//import com.alibaba.nacos.api.exception.NacosException;
//import com.alibaba.nacos.api.naming.NamingService;
//import com.alibaba.nacos.api.naming.pojo.Instance;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RestController;
//
///**
// * @Author hxy
// * @Date 2022/4/13
// */
//
//@RestController
//public class TestAddress {
//
//
//    @RequestMapping(value = "/address", method = RequestMethod.GET)
//    public String getGatewayAddress() {
//        String res = null;
//        try {
//            NamingService namingService = nacosServiceManager.getNamingService(nacosDiscoveryProperties.getNacosProperties());
//            Instance instance = namingService.selectOneHealthyInstance("service-provider");
//
//            res = instance.getIp() + ":" + instance.getPort();
//            return res;
//        } catch (NacosException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//}