package cn.edu.hubu.rpc.core.remoting.params;

import cn.edu.hubu.rpc.core.remoting.message.RpcRequestMessage;

/**
 * @Author hxy
 * @Date 2022/4/11
 *
 *  beat for keep-alive
 */

public class Beat {

    public static final int BEAT_INTERVAL = 30;
    public static final String BEAT_ID = "BEAT_PING_PONG";

    public static RpcRequestMessage BEAT_PING;

    static {
        BEAT_PING = new RpcRequestMessage(){};
        BEAT_PING.setRequestId(BEAT_ID);
    }
}
