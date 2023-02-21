package com.friendship.config;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class WebSocketConfig {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Bean
    public ServerEndpointExporter serverEndpointExporter(){
        return new ServerEndpointExporter();
    }

    @Bean
    public ScheduledExecutorService threadPool(){
        return Executors.newScheduledThreadPool(2);
    }

    @Bean
    public Gson gson() {
        return new Gson();
    }

    @Bean
    public HashOperations hashOperations(){
       return stringRedisTemplate.opsForHash();
    }
}
