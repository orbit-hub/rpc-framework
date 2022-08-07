package cn.edu.hubu.rpc.core.remoting.route.loadbalance;

import cn.edu.hubu.rpc.core.remoting.route.RpcLoadBalance;

import java.util.Random;
import java.util.TreeSet;

/**
 * @Author hxy
 * @Date 2022/4/18
 *
 * Random
 */

public class RpcLoadBalanceRandomStrategy extends RpcLoadBalance {

    private Random random = new Random();
    @Override
    public String route(String serviceKey, TreeSet<String> addressSet) {
        // arr
        String[] addressArr = addressSet.toArray(new String[addressSet.size()]);

        // random
        String finalAddress = addressArr[random.nextInt(addressSet.size())];
        return finalAddress;
    }
}
