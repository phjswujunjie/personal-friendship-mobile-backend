package com.friendship.service.impl;

import com.friendship.mapper.ChatMessageMapper;
import com.friendship.mapper.FriendlyRelationshipMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
}
