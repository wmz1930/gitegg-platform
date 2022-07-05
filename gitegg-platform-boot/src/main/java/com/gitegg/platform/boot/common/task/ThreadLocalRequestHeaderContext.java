package com.gitegg.platform.boot.common.task;

import java.util.Map;

/**
 * 异步线程池 父子线程传递上下文，请注意：线程池中使用TaskDecorator装饰器时，使用ThreadLocal即可，不要使用TransmittableThreadLocal，否则会失效
 * @author GitEgg
 * @date 2022/7/3
 */
public class ThreadLocalRequestHeaderContext {

    public static final ThreadLocal<Map<String, String>> threadLocalRequestHeader = new ThreadLocal<>();

    public static void set(Map<String, String> map) {
        threadLocalRequestHeader.set(map);
    }

    public static Map<String, String> get() {
        return threadLocalRequestHeader.get();
    }

    public static void remove() {
        threadLocalRequestHeader.remove();
    }
}
