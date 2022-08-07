package cn.edu.hubu.rpc.core.utils;

/**
 * @Author hxy
 * @Date 2022/4/6
 */

public class RpcException extends RuntimeException {
    private static final long serialVersionUID = 288L;

    public RpcException(String msg) {
        super(msg);
    }

    public RpcException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public RpcException(Throwable cause) {
        super(cause);
    }
}
