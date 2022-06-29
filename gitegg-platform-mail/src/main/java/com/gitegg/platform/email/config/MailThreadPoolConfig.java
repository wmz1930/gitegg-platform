package com.gitegg.platform.email.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author GitEgg
 * @date 2022/6/25
 */
@Configuration
public class MailThreadPoolConfig {

    @Value("${spring.mail-task.execution.pool.core-size}")
    private int corePoolSize;

    @Value("${spring.mail-task.execution.pool.max-size}")
    private int maxPoolSize;

    @Value("${spring.mail-task.execution.pool.queue-capacity}")
    private int queueCapacity;

    @Value("${spring.mail-task.execution.thread-name-prefix}")
    private String namePrefix;

    @Value("${spring.mail-task.execution.pool.keep-alive}")
    private int keepAliveSeconds;


    @Bean("AsyncSendMailTaskExecutor")
    public AsyncTaskExecutor mailTaskExecutor(){

        ThreadPoolTaskExecutor executor=new ThreadPoolTaskExecutor();
        //最大线程数
        executor.setMaxPoolSize(maxPoolSize);
        //核心线程数
        executor.setCorePoolSize(corePoolSize);
        //任务队列的大小
        executor.setQueueCapacity(queueCapacity);
        //线程前缀名
        executor.setThreadNamePrefix(namePrefix);
        //线程存活时间
        executor.setKeepAliveSeconds(keepAliveSeconds);

        /**
         * 拒绝处理策略
         * CallerRunsPolicy()：交由调用方线程运行，比如 main 线程。
         * AbortPolicy()：直接抛出异常。
         * DiscardPolicy()：直接丢弃。
         * DiscardOldestPolicy()：丢弃队列中最老的任务。
         */
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        //线程初始化
        executor.initialize();

        return executor;
    }
}
