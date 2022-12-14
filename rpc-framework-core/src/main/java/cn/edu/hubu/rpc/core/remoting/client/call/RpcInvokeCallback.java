package cn.edu.hubu.rpc.core.remoting.client.call;

/**
 * @Author hxy
 * @Date 2022/4/13
 */

public abstract class RpcInvokeCallback<T> {

    public abstract void onSuccess(T result);

    public abstract void onFailure(Throwable exception);

    // ---------------------- thread invoke callback ----------------------

    private static ThreadLocal<RpcInvokeCallback> threadInvokerFuture = new ThreadLocal<RpcInvokeCallback>();

    /**
     * get callback
     *
     * @return
     */
    public static RpcInvokeCallback getCallback() {
        RpcInvokeCallback invokeCallback = threadInvokerFuture.get();
        threadInvokerFuture.remove();
        return invokeCallback;
    }

    /**
     * set future
     *
     * @param invokeCallback
     */
    public static void setCallback(RpcInvokeCallback invokeCallback) {
        threadInvokerFuture.set(invokeCallback);
    }

    /**
     * remove future
     */
    public static void removeCallback() {
        threadInvokerFuture.remove();
    }


}
