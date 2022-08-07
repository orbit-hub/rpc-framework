package cn.edu.hubu.rpc.core.remoting.message;


import cn.edu.hubu.rpc.core.remoting.client.RpcInvokerFactory;
import cn.edu.hubu.rpc.core.remoting.client.call.RpcInvokeCallback;
import cn.edu.hubu.rpc.core.utils.RpcException;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @Author hxy
 * @Date 2022/4/7
 */

public class RpcFutureResponse implements Future<RpcResponseMessage> {

    private RpcInvokerFactory invokerFactory;

    // net data
    private RpcRequestMessage request;
    private RpcResponseMessage response;

    // future lock
    private boolean done = false;
    private Object lock = new Object();

    // callback, can be null
    private RpcInvokeCallback invokeCallback;

    public RpcFutureResponse(final RpcInvokerFactory invokerFactory, RpcRequestMessage request, RpcInvokeCallback invokeCallback) {
        this.invokerFactory = invokerFactory;
        this.request = request;
        this.invokeCallback = invokeCallback;

        // set-InvokerFuture
        setInvokerFuture();
    }
    // ---------------------- response pool ----------------------
    public void setInvokerFuture(){
        this.invokerFactory.setInvokerFuture(request.getRequestId(), this);
    }
    public void removeInvokerFuture(){
        this.invokerFactory.removeInvokerFuture(request.getRequestId());
    }

    // ---------------------- get ----------------------
    public RpcRequestMessage getRequest() {
        return request;
    }
    public RpcInvokeCallback getInvokeCallback() {
        return invokeCallback;
    }

    // ---------------------- for invoke back ----------------------
    public void setResponse(RpcResponseMessage response) {
        this.response = response;
        synchronized (lock) {
            done = true;
            lock.notifyAll();
        }
    }
    // ---------------------- for invoke ----------------------
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public RpcResponseMessage get() throws InterruptedException, ExecutionException {
        try {
            return get(-1, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            throw new RpcException(e);
        }
    }

    @Override
    public RpcResponseMessage get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (!done) {
            synchronized (lock) {
                try {
                    if (timeout < 0) {
                        lock.wait();
                    } else {
                        long timeoutMillis = (TimeUnit.MILLISECONDS==unit)?timeout:TimeUnit.MILLISECONDS.convert(timeout , unit);
                        lock.wait(timeoutMillis);
                    }
                } catch (InterruptedException e) {
                    throw e;
                }
            }
        }

        if (!done) {
            throw new RpcException("rpc, request timeout at:"+ System.currentTimeMillis() +", request:" + request.toString());
        }
        return response;
    }
}
