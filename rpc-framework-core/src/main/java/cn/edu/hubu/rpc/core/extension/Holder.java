package cn.edu.hubu.rpc.core.extension;

/**
 * @Author hxy
 * @Date 2022/4/23
 */

public class Holder<T> {
    private volatile T value;

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }
}