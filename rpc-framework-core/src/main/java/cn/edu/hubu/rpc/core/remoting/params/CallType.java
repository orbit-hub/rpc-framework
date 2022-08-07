package cn.edu.hubu.rpc.core.remoting.params;

/**
 * @Author hxy
 * @Date 2022/4/7
 */

public enum CallType {
    SYNC;


    public static CallType match(String name, CallType defaultCallType){
        for (CallType item : CallType.values()) {
            if (item.name().equals(name)) {
                return item;
            }
        }
        return defaultCallType;
    }

}