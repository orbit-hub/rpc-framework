package cn.edu.hubu.rpc.core.registry;


import cn.edu.hubu.rpc.core.extension.SPI;
import com.alibaba.nacos.api.exception.NacosException;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @Author hxy
 * @Date 2022/4/6
 *
 * application registry
 */
@SPI
public interface Register {

    /**
     * start
     */
    public abstract void start(Map<String, String> param) throws NacosException;

    /**
     * stop
     */
    public abstract void stop() throws NacosException;


    /**
     * registry service, for mult
     *
     * @param keys      service key
     * @param value     service value/ip:port
     * @return
     */
    public abstract boolean registry(Set<String> keys, String value) throws NacosException;


    /**
     * remove service, for mult
     *
     * @param keys
     * @param value
     * @return
     */
    public abstract boolean remove(Set<String> keys, String value) throws NacosException;

    /**
     * discovery services, for mult
     *
     * @param keys
     * @return
     */
    public abstract Map<String, TreeSet<String>> discovery(Set<String> keys) throws NacosException;

    /**
     * discovery service, for one
     *
     * @param key   service key
     * @return      service value/ip:port
     */
    public abstract TreeSet<String> discovery(String key) throws NacosException;

}
