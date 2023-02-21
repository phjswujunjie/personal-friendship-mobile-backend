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
        System.out.println("FriendshipWebSocket连接成功!!!!");
    }

    //收到信息触发
    @OnMessage
    public void onMessage(String message, Session session) throws Exception {
        Gson g = new Gson();
        Map map = g.fromJson(message, Map.class);
        System.out.println("传过来的数据" + message);
        if (map.size() == 2) {
            //用户点击了某个朋友的聊天窗口则将所有的未读信息清除
            userId = Long.valueOf((String) map.get("userId"));
            Long toId = Long.valueOf((String) map.get("toId"));
            ChatMessageContainer.userMap.get(userId).setSession(session);
            if (ChatMessageContainer.messageMap.get(userId) != null
                    &&
                    ChatMessageContainer.messageMap.get(userId).get(toId) != null) {
                ChatMessageContainer.messageMap.get(userId).get(toId).clear();
            }
        } else {
            //发信息给对应的用户(用户也在跟自己聊), 否则将未读信息存到对应的未读信息容器中
            ChatMessage chatMessage = g.fromJson(message, ChatMessage.class);
            chatMessage.setUserId(Long.valueOf((String) map.get("userId")));
            chatMessage.setToId(Long.valueOf((String) map.get("toId")));
            chatMessage.setCreateTime(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()));
            chatMessage.setIsDelete(0);
            chatMessageMapper.insert(chatMessage);
            FriendshipWebSocket toUser = ChatMessageContainer.userMap.get(chatMessage.getToId());
            Map<String, Object> messageMap = new HashMap<>();
            // 将ChatMessage的信息转换到Map中
            processMap(messageMap, chatMessage);
            if (toUser != null && toUser.getSession() != null) {
                // 将信息利用webSocket推送到目标用户
                toUser.getSession().getBasicRemote().sendText(g.toJson(messageMap));
            } else {
                // 储存未读信息
                Long toId = chatMessage.getToId();
                if (ChatMessageContainer.messageMap.get(toId) == null) {
                    List<Map<String, Object>> messageList = Collections.synchronizedList(new ArrayList<>());
                    messageList.add(messageMap);
                    Map<Long, List<Map<String, Object>>> unreadMessageMap = new ConcurrentHashMap<>();
                    unreadMessageMap.put(chatMessage.getUserId(), messageList);
                    ChatMessageContainer.messageMap.put(toId, unreadMessageMap);
                } else if (ChatMessageContainer.messageMap.get(toId).get(chatMessage.getUserId()) == null) {
                    List<Map<String, Object>> messageList = Collections.synchronizedList(new ArrayList<>());
                    messageList.add(messageMap);
                    ChatMessageContainer.messageMap.get(toId).put(chatMessage.getUserId(), messageList);
                } else {
                    ChatMessageContainer.messageMap.get(toId).get(chatMessage.getUserId()).add(messageMap);
                }
            }
        }
    }

    //连接关闭触发
    @OnClose
    public void onClose(Session session) {
        System.out.println("FriendshipWebSocket离开");
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.out.println("错误代码");
        throwable.printStackTrace();
    }

    private void processMap(Map<String, Object> messageMap, ChatMessage chatMessage) {
        messageMap.put("content", chatMessage.getContent());
        messageMap.put("createTime", chatMessage.getCreateTime());
        messageMap.put("avatar", "http://localhost:8888/static/upload/" +
                stringRedisTemplate.opsForHash().get("user_" + chatMessage.getUserId(), "avatar"));
        messageMap.put("userId", chatMessage.getUserId());
        messageMap.put("toId", chatMessage.getToId());
        messageMap.put("isDelete", chatMessage.getIsDelete());
        messageMap.put("nickname",  stringRedisTemplate.opsForHash().get("user_" + chatMessage.getUserId(), "nickname"));
        messageMap.put("media", chatMessage.getMedia());
        messageMap.put("id", chatMessage.getId());
    }
}
