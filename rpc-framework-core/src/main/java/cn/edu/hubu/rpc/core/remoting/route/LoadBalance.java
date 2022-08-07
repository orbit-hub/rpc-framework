package cn.edu.hubu.rpc.core.remoting.route;

import cn.edu.hubu.rpc.core.remoting.route.loadbalance.*;

/**
 * @Author hxy
 * @Date 2022/4/7
 */

public enum LoadBalance {

    RANDOM(new RpcLoadBalanceRandomStrategy()),
    ROUND(new RpcLoadBalanceRoundStrategy()),
    LRU(new RpcLoadBalanceLRUStrategy()),
    LFU(new RpcLoadBalanceLFUStrategy()),
    CONSISTENT_HASH(new RpcLoadBalanceConsistentHashStrategy());
    public final RpcLoadBalance rpcInvokerRouter;

    private LoadBalance(RpcLoadBalance rpcInvokerRouter) {
        this.rpcInvokerRouter = rpcInvokerRouter;
    }

    public static LoadBalance match(String name, LoadBalance defaultRouter) {
        for (LoadBalance item : LoadBalance.values()) {
            if (item.name().equals(name)) {
                return item;
            }
        }
        return defaultRouter;
    }

}
