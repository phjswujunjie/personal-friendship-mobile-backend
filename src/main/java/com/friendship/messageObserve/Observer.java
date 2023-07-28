package com.friendship.messageObserve;

import java.io.IOException;
import java.util.Map;

public abstract class Observer {

    protected Long userId;

    // 接收消息并开始发送的方法
    public abstract void receive(Map<String, Object> map) throws Exception;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
