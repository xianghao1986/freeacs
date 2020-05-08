package com.github.freeacs.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.Executor;

@Configuration
public class ExecutorConfig {
    /** Set the ThreadPoolExecutor's core pool size. */
    @Value("${executor.corePoolSize:10}")
    private int corePoolSize;
    /** Set the ThreadPoolExecutor's maximum pool size. */
    @Value("${executor.maxPoolSize:100}")
    private int maxPoolSize;
    /** Set the capacity for the ThreadPoolExecutor's BlockingQueue. */
    @Value("${executor.queueCapacity:10}")
    private int queueCapacity;

    @Value("${scheduler.poolSize:10}")
    private int schedulerPoolSize;

    @Bean
	public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("tr069-executor-");
        executor.initialize();
        return executor;
    }

//	@Bean
//	public TaskScheduler taskScheduler() {
//		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
//		scheduler.setPoolSize(schedulerPoolSize);
//		return scheduler;
//	}
}
