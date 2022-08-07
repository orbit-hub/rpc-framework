package cn.edu.hubu.rpc.core.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @Author hxy
 * @Date 2022/4/12
 *
 * 获取IP方法
 */

public class IpUtils {

    public static String getIp(){
        try {
            return InetAddress.getLocalHost().getHostAddress();
        }
        catch (UnknownHostException e) {
        }
        return "127.0.0.1";
    }

    public static String getIpPort(String ip, int port){
        if (ip==null) {
            return null;
        }
        return ip.concat(":").concat(String.valueOf(port));
    }


    public static Object[] parseIpPort(String address){
        String[] array = address.split(":");

        String host = array[0];
        int port = Integer.parseInt(array[1]);

        return new Object[]{host, port};
    }

}
