package com.friendship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.friendship.mapper.ChatMessageMapper;
import com.friendship.mapper.FriendlyRelationshipMapper;
import com.friendship.pojo.ChatMessage;
import com.friendship.utils.CommonString;
import com.friendship.websocket.ChatMessageContainer;
import com.friendship.websocket.FriendshipWebSocket;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatMessageService {

    @Resource
    private ChatMessageMapper chatMessageMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private FriendlyRelationshipMapper friendlyRelationshipMapper;

    @Resource
    private Gson gson;


    /**
     * 处理用户发送的信息
     *
     * @param chatMessage
     * @throws IOException
     */
    public Map<String, Object> sendMessage(ChatMessage chatMessage, String messageConfirmId) throws IOException {
        chatMessage.setCreateTime(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()));
        chatMessage.setIsDelete(0);
        if (ChatMessageContainer.messageConfirmList.contains(messageConfirmId)) {
            return new HashMap<>();
        }
        synchronized (this) {
            if (ChatMessageContainer.messageConfirmList.contains(messageConfirmId)) {
                return new HashMap<>();
            }
            chatMessageMapper.insert(chatMessage);
            ChatMessageContainer.messageConfirmList.add(messageConfirmId);
        }
        FriendshipWebSocket toUser = ChatMessageContainer.userMap.get(chatMessage.getToId());
        Map<String, Object> messageMap = new HashMap<>();
        // 将ChatMessage的信息转换到Map中
        processMap(messageMap, chatMessage);
        if (chatMessage.getReplyId() != -1) {
            QueryWrapper<ChatMessage> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("id", chatMessage.getReplyId());
            List<Map<String, Object>> list = chatMessageMapper.selectMaps(queryWrapper);
            list.get(0).put("nickname", stringRedisTemplate.opsForHash().get("user_" + list.get(0).get("user_id"), "nickname"));
            messageMap.put("replyMessage", list.get(0));
        }
        if (toUser != null && toUser.getSession() != null) {
            // 将信息利用webSocket推送到目标用户
            toUser.getSession().getBasicRemote().sendText(gson.toJson(messageMap));
        } else {
            // 储存未读信息
            Long toId = chatMessage.getToId();
            if (ChatMessageContainer.messageMap.get(toId) == null) {
                // 可能有多个用户会同时给用户发信息, 高并发的状态下会创建多个容器, 上锁
                synchronized (FriendshipWebSocket.class) {
                    List<Map<String, Object>> messageList = Collections.synchronizedList(new ArrayList<>());
                    messageList.add(messageMap);
                    Map<Long, List<Map<String, Object>>> unreadMessageMap = new ConcurrentHashMap<>();
                    unreadMessageMap.put(chatMessage.getUserId(), messageList);
                    ChatMessageContainer.messageMap.put(toId, unreadMessageMap);
                }
            } else if (ChatMessageContainer.messageMap.get(toId).get(chatMessage.getUserId()) == null) {
                // 这里不需要上锁, 因为用户之间的聊天为1对1的关系, 无论如何也只会创建一个相同的容器
                List<Map<String, Object>> messageList = Collections.synchronizedList(new ArrayList<>());
                messageList.add(messageMap);
                ChatMessageContainer.messageMap.get(toId).put(chatMessage.getUserId(), messageList);
            } else {
                ChatMessageContainer.messageMap.get(toId).get(chatMessage.getUserId()).add(messageMap);
            }
        }
        messageMap.put("avatar", CommonString.RESOURCES_ADDRESS + (stringRedisTemplate.opsForHash().get("user_" + messageMap.get("userId"), "avatar")));
        messageMap.put("nickname", stringRedisTemplate.opsForHash().get("user_" + messageMap.get("userId"), "nickname"));
        messageMap.put("id", chatMessage.getId());
        return messageMap;
    }

    public List<Map<String, Object>> getChatMessage(Long toId, String token) {
        Long userId = Long.valueOf(Objects.requireNonNull(stringRedisTemplate.opsForValue().get(token)));
        //确保聊天的用户id为自己好友的id, 而不是一个乱输入的id
        List<Map<String, Object>> mapList = friendlyRelationshipMapper.queryRelation(userId, toId);
        if (mapList.size() != 2) {
            return null;
        }
        List<Map<String, Object>> messageList = new java.util.ArrayList<>(chatMessageMapper.getChatMessages(userId, toId).stream().map(p -> {
            p.put("avatar", CommonString.RESOURCES_ADDRESS + (stringRedisTemplate.opsForHash().get("user_" + p.get("userId"), "avatar")));
            p.put("nickname", stringRedisTemplate.opsForHash().get("user_" + p.get("userId"), "nickname"));
            return p;
        }).toList());
        for (Map<String, Object> map : messageList) {
            if (Long.parseLong(map.get("replyId") + "") != -1) {
                for (Map<String, Object> m : messageList) {
                    if (Long.valueOf(map.get("replyId") + "").equals(Long.valueOf(m.get("id") + ""))) {
                        map.put("replyMessage", transfer(m, new HashMap<>()));
                    }
                }
            }
        }
        Map<String, Object> map = new HashMap<>();
        map.put("toNickname", stringRedisTemplate.opsForHash().get("user_" + toId, "nickname"));
        map.put("selfId", userId);
        map.put("selfAvatar", CommonString.RESOURCES_ADDRESS + (stringRedisTemplate.opsForHash().get("user_" + userId, "avatar")));
        messageList.add(0, map);
        return messageList;
    }

    public void storageUnreadChatMessage(HttpServletRequest request, String content, Long fromId, String createTime, String avatar) {
        Long toId = Long.valueOf(Objects.requireNonNull(stringRedisTemplate.opsForValue().get(request.getHeader("token"))));
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("content", content);
        messageMap.put("createTime", createTime);
        messageMap.put("fromUserAvatar", avatar);
        messageMap.put("fromId", fromId);
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

    private void processMap(Map<String, Object> messageMap, ChatMessage chatMessage) {
        messageMap.put("content", chatMessage.getContent());
        messageMap.put("replyId", chatMessage.getReplyId());
        messageMap.put("createTime", chatMessage.getCreateTime());
        messageMap.put("avatar", CommonString.RESOURCES_ADDRESS +
                stringRedisTemplate.opsForHash().get("user_" + chatMessage.getUserId(), "avatar"));
        messageMap.put("userId", chatMessage.getUserId());
        messageMap.put("toId", chatMessage.getToId());
        messageMap.put("isDelete", chatMessage.getIsDelete());
        messageMap.put("nickname", stringRedisTemplate.opsForHash().get("user_" + chatMessage.getUserId(), "nickname"));
        messageMap.put("media", chatMessage.getMedia());
        messageMap.put("id", chatMessage.getId());
    }

    private Map<String, Object> transfer(Map<String, Object> src, Map<String, Object> target) {
        target.put("content", src.get("content"));
        target.put("createTime", src.get("createTime"));
        target.put("toId", src.get("toId"));
        target.put("userId", src.get("userId"));
        target.put("nickname", src.get("nickname"));
        target.put("isDelete", src.get("isDelete"));
        target.put("media", src.get("media"));
        target.put("id", src.get("id"));
        return target;
    }
}
