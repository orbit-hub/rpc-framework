package cn.edu.hubu.rpc.core.tolerant;

import java.util.concurrent.TimeUnit;

/**
 * @Author hxy
 * @Date 2022/4/23
 */

public abstract class CounterLimit {


    protected int limitCount;

    protected long limitTime;

    protected TimeUnit timeUnit;

    protected volatile boolean isLimited;

    protected abstract boolean tryCount();
}
