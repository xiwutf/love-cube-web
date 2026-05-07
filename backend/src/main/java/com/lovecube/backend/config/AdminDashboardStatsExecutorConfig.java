package com.lovecube.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 管理端仪表盘「7 日趋势」并行查询专用线程池，避免阻塞 servlet 线程且可控并发。
 */
@Configuration
public class AdminDashboardStatsExecutorConfig {

    private static final int POOL_SIZE = 8;
    private static final AtomicInteger THREAD_NO = new AtomicInteger();

    @Bean(destroyMethod = "shutdown")
    public ExecutorService adminDashboardTrendsExecutor() {
        return Executors.newFixedThreadPool(POOL_SIZE, r -> {
            Thread t = new Thread(r, "admin-dashboard-trends-" + THREAD_NO.incrementAndGet());
            t.setDaemon(true);
            return t;
        });
    }
}
