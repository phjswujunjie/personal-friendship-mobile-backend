package com.friendship.websocket;

import com.friendship.mapper.ChatMessageMapper;
import com.friendship.pojo.ChatMessage;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

//操作用户聊天信息的websocket
@ServerEndpoint("/chatWebSocket")
@Component
@CrossOrigin(originPatterns = {"*"}, allowCredentials = "true")
public class FriendshipWebSocket {

    private static ChatMessageMapper chatMessageMapper;

    private static StringRedisTemplate stringRedisTemplate;

    @Autowired
    public void setChatMessageMapper(ChatMessageMapper chatMessageMapper) {
        FriendshipWebSocket.chatMessageMapper = chatMessageMapper;
    }

    @Autowired
    public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
        FriendshipWebSocket.stringRedisTemplate = stringRedisTemplate;
    }

    private Session session;

    private Long userId;

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    //连接建立触发
    @OnOpen
    public void onOpen(Session session) {
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    //收到信息触发
    @OnMessage
    public void onMessage(String message, Session session) throws Exception {
        Gson g = new Gson();
        Map map = g.fromJson(message, Map.class);
        if (map.size() == 2) {
            //用户点击了某个朋友的聊天窗口则将所有的未读信息清除
            userId = Long.valueOf((String) map.get("userId"));
            Long toId = Long.valueOf((String) map.get("toId"));
            if (ChatMessageContainer.userMap.get(userId) == null) {
                synchronized (SaveLoginInfoWebSocket.class) {
                    FriendshipWebSocket friendshipWebSocket = new FriendshipWebSocket();
                    friendshipWebSocket.setUserId(userId);
                    friendshipWebSocket.setSession(session);
                    ChatMessageContainer.userMap.put(userId, friendshipWebSocket);
                }
            } else {
                ChatMessageContainer.userMap.get(userId).setSession(session);
            }
            if (ChatMessageContainer.messageMap.get(userId) != null
                    &&
                    ChatMessageContainer.messageMap.get(userId).get(toId) != null) {
                ChatMessageContainer.messageMap.get(userId).get(toId).clear();
            }
        }
    }

    //连接关闭触发
    @OnClose
    public void onClose(Session session) {
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        throwable.printStackTrace();
    }
}
