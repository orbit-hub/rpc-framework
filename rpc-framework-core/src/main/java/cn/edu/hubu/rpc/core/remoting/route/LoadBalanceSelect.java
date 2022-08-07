package cn.edu.hubu.rpc.core.remoting.route;


import cn.edu.hubu.rpc.core.extension.SPI;

import java.util.TreeSet;

/**
 * @Author hxy
 * @Date 2022/4/7
 *
 * Interface to the load balancing policy
 */
@SPI
public interface LoadBalanceSelect {
    /**
     * Choose one from the list of existing service addresses list
     *
     * @param serviceUrlList Service address list
     * @param rpcRequest
     * @return target service address
     */
    /**
     * Choose one from the list of existing service addresses list
     * @param serviceKey
     * @param addressSet
     * @return
     */
    String selectServiceAddress(String serviceKey, TreeSet<String> addressSet);
}
