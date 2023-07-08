package com.gitegg.platform.boot.common.task;

import com.gitegg.platform.boot.util.GitEggWebUtils;
import org.springframework.core.task.TaskDecorator;
import org.springframework.util.CollectionUtils;

import java.util.Map;

/**
 * 异步线程池装饰器，使用ThreadLocal即可，不要使用TransmittableThreadLocal，否则会失效
 * @author GitEgg
 * @date 2022/7/3
 */
public class RequestHeaderTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        try {
            Map<String,String> requestHeaders = GitEggWebUtils.getHeaders(GitEggWebUtils.getRequest());
            Map<String, String> mapLocal = ThreadLocalRequestHeaderContext.get();
            if (!CollectionUtils.isEmpty(mapLocal))
            {
                requestHeaders.putAll(mapLocal);
            }
            return () -> {
                try {
                    // 将父线程的requestHeaders设置进子线程里
                    ThreadLocalRequestHeaderContext.set(requestHeaders);
                    // 执行子线程方法
                    runnable.run();
                } finally {
                    // 清除线程requestHeaders的值
                    ThreadLocalRequestHeaderContext.remove();
                }
            };
        } catch (IllegalStateException e) {
            return runnable;
        }
    }
}
