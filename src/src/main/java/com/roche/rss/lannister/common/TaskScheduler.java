package com.roche.rss.lannister.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.lang.annotation.Retention;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Configuration
/**
 * register ThreadPoolTaskScheduler
 */
public class TaskScheduler {
    private final Logger log = LoggerFactory.getLogger(TaskScheduler.class);
    private final int numThreads = 8;

    @Bean
    /**
     * initialize ThreadPoolTaskScheduler with numThreads
     */
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(numThreads);
        threadPoolTaskScheduler.setThreadNamePrefix("ThreadPoolTaskScheduler");
        return threadPoolTaskScheduler;
    }

    @Bean
    /**
     * initialize ExecutorService with numThreads
     */
    public ExecutorService threadPoolexecutorService(){
        ExecutorService es = Executors.newFixedThreadPool(numThreads);
        return es;
    }

}
