package cn.edu.hubu.rpc.core.remoting.filter;

public interface RpcGenericService {

    /**
     * generic invoke
     *
     * @param iface                 iface name
     * @param version               iface version
     * @param method                method name
     * @param parameterTypes        parameter types, limit base type like "int、java.lang.Integer、java.util.List、java.util.Map ..."
     * @param args
     * @return
     */
    public Object invoke(String iface, String version, String method, String[] parameterTypes, Object[] args);

}
