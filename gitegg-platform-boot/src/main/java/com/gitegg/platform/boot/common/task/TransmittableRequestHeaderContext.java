package com.gitegg.platform.boot.common.task;

import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.Map;

/**
 * 异步线程池 父子线程传递上下文，请注意：线程池中使用TaskDecorator，也可实现，这里保留两种方式，实际过程中请不要和TaskDecorator混用
 * 当使用TtlExecutors.getTtlExecutor(executor);装饰线程池时，需要使用TransmittableThreadLocal
 * @author GitEgg
 * @date 2022/7/3
 */
public class TransmittableRequestHeaderContext {

    public static final ThreadLocal<Map<String, String>> transmittableRequestHeader = new TransmittableThreadLocal<>();

    public static void set(Map<String, String> map) {
        transmittableRequestHeader.set(map);
    }

    public static Map<String, String> get() {
        return transmittableRequestHeader.get();
    }

    public static void remove() {
        transmittableRequestHeader.remove();
    }
}
