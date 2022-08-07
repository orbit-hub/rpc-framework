package cn.edu.hubu.rpc.core.remoting.route.loadbalance;

import cn.edu.hubu.rpc.core.remoting.route.RpcLoadBalance;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @Author hxy
 * @Date 2022/4/18
 *
 * lru
 */

public class RpcLoadBalanceLRUStrategy extends RpcLoadBalance {

    private ConcurrentMap<String, LinkedHashMap<String, String>> LRUMap = new ConcurrentHashMap<String, LinkedHashMap<String, String>>();
    private long CACHE_VALID_TIME = 0;

    private String doRoute(String serviceKey, TreeSet<String> addressSet) {
        // cache clear
        if (System.currentTimeMillis() > CACHE_VALID_TIME) {
            LRUMap.clear();
            CACHE_VALID_TIME = System.currentTimeMillis() + 1000*60*60*24;
        }

        //init lru
        LinkedHashMap<String, String> lruItem = LRUMap.get(serviceKey);
        lruItem = new LinkedHashMap<String, String>(16, 0.75f, true){
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
                if(super.size() > 1000){
                    return true;
                }else{
                    return false;
                }
            }
        };
        LRUMap.putIfAbsent(serviceKey, lruItem);

        // put new
        for (String address: addressSet) {
            if (!lruItem.containsKey(address)) {
                lruItem.put(address, address);
            }
        }
        // remove old
        List<String> delKeys = new ArrayList<>();
        for (String existKey: lruItem.keySet()) {
            if (!addressSet.contains(existKey)) {
                delKeys.add(existKey);
            }
        }
        if (delKeys.size() > 0) {
            for (String delKey: delKeys) {
                lruItem.remove(delKey);
            }
        }

        // load
        String eldestKey = lruItem.entrySet().iterator().next().getKey();
        String eldestValue = lruItem.get(eldestKey);
        return eldestValue;
    }


    @Override
    public String route(String serviceKey, TreeSet<String> addressSet) {
        String finalAddress = doRoute(serviceKey, addressSet);
        return finalAddress;
    }


}
