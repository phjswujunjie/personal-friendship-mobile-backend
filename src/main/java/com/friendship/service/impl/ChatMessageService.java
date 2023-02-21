package com.friendship.service.impl;

import com.friendship.mapper.ChatMessageMapper;
import com.friendship.mapper.FriendlyRelationshipMapper;
import com.friendship.websocket.ChatMessageContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatMessageService {

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private FriendlyRelationshipMapper friendlyRelationshipMapper;

    public List<Map<String, Object>> getChatMessage(Long toId, String token){
        Long userId = Long.valueOf(Objects.requireNonNull(stringRedisTemplate.opsForValue().get(token)));
        //确保聊天的用户id为自己好友的id, 而不是一个乱输入的id
        List<Map<String, Object>> mapList = friendlyRelationshipMapper.queryRelation(userId, toId);
        if (mapList.size() != 2){
            return null;
        }
        List<Map<String, Object>> messageList = new java.util.ArrayList<>(chatMessageMapper.getChatMessages(userId, toId).stream().map(p -> {
            p.put("avatar", "http://localhost:8888/static/upload/" + (stringRedisTemplate.opsForHash().get("user_" + p.get("userId"), "avatar")));
            p.put("nickname", stringRedisTemplate.opsForHash().get("user_" + p.get("userId"), "nickname"));
            return p;
        }).toList());
        Map<String, Object> map = new HashMap<>();
        map.put("toNickname", stringRedisTemplate.opsForHash().get("user_"+ toId, "nickname"));
        map.put("selfId", userId);
        map.put("selfAvatar", "http://localhost:8888/static/upload/" + (stringRedisTemplate.opsForHash().get("user_"+ userId, "avatar")));
        messageList.add(0, map);
        return messageList;
    }

    public void storageUnreadChatMessage(HttpServletRequest request, String content, Long fromId, String createTime, String avatar) {
        Long toId = Long.valueOf(Objects.requireNonNull(stringRedisTemplate.opsForValue().get(request.getHeader("token"))));
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("content", content);
        messageMap.put("createTime", createTime);
        messageMap.put("fromUserAvatar", avatar);
        messageMap.put("fromId",fromId);
        if (Optional.ofNullable(ChatMessageContainer.messageMap.get(toId)).isEmpty()) {
            List<Map<String, Object>> messageList = Collections.synchronizedList(new ArrayList<>());
            messageList.add(messageMap);
            Map<Long, List<Map<String, Object>>> unreadMessageMap = new ConcurrentHashMap<>();
            unreadMessageMap.put(fromId, messageList);
            ChatMessageContainer.messageMap.put(toId, unreadMessageMap);
        } else if (Optional.ofNullable(ChatMessageContainer.messageMap.get(toId).get(fromId)).isEmpty()) {
            List<Map<String, Object>> messageList = Collections.synchronizedList(new ArrayList<>());
            messageList.add(messageMap);
            ChatMessageContainer.messageMap.get(toId).put(fromId, messageList);
        } else {
            ChatMessageContainer.messageMap.get(toId).get(fromId).add(messageMap);
        }
    }
}
