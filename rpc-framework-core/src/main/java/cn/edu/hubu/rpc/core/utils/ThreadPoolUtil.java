package cn.edu.hubu.rpc.core.utils;

import java.util.concurrent.*;

/**
 * @Author hxy
 * @Date 2022/4/12
 */

public class ThreadPoolUtil {

    /**
     * make server thread pool
     *
     * @param serverType
     * @return
     */
    public static ThreadPoolExecutor makeServerThreadPool(final String serverType, int corePoolSize, int maxPoolSize){
        return new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(1000),
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "rpc, "+serverType+"-serverHandlerPool-" + r.hashCode());
                    }
                },
                new RejectedExecutionHandler() {
                    @Override
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                        throw new RpcException("rpc "+serverType+" Thread pool is EXHAUSTED!");
                    }
                });		// default maxThreads 300, minThreads 60


    }
}
