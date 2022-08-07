package cn.edu.hubu.rpc.core.remoting.route;


import com.alibaba.nacos.common.utils.CollectionUtils;

import java.util.TreeSet;

/**
 * @Author hxy
 * @Date 2022/4/7
 *
 *
 */

public abstract class RpcLoadBalance implements LoadBalanceSelect {

    @Override
    public String selectServiceAddress(String serviceKey, TreeSet<String> addressSet) {
        if (CollectionUtils.isEmpty(addressSet)) {
            return null;
        }
        if (addressSet.size() == 1) {
            return addressSet.pollFirst();
        }
        return route(serviceKey, addressSet);
    }

    public abstract String route(String serviceKey, TreeSet<String> addressSet);
}
