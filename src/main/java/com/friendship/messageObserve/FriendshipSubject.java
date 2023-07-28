package com.friendship.messageObserve;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class FriendshipSubject implements Subject{

    private final Map<Long, Observer> observerMap = new ConcurrentHashMap<>();

    private static final FriendshipSubject instance = new FriendshipSubject();

    private FriendshipSubject(){
    }

    @Bean
    public static FriendshipSubject getInstance() {
        return instance;
    }

    @Override
    public void register(Observer observer) {
        observerMap.put(observer.getUserId(), observer);
    }

    @Override
    public void remove(Observer observer) {
        observerMap.remove(observer.getUserId());
    }

    @Override
    public void notifyObservers(Map<String, Object> map) {
    }
}
