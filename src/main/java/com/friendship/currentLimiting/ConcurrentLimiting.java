package com.friendship.currentLimiting;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class ConcurrentLimiting implements ApplicationRunner {
    @Autowired
    private ScheduledExecutorService scheduledExecutorService;

    /**
     *  计数器限流
     * @param args
     * @throws Exception
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        scheduledExecutorService.scheduleWithFixedDelay(() -> {
           ProcessLimit.limitingMap.values().forEach(p -> {
               p.updateAndGet(x -> x = 0);
           });
        }, 1, 1, TimeUnit.SECONDS);
    }
}
