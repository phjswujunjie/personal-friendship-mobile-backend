package com.friendship.websocket;

import com.friendship.mapper.GroupMessageMapper;
import com.friendship.mapper.UserGroupRelationMapper;
import com.friendship.pojo.GroupMessage;
import com.friendship.pojo.UserGroupRelation;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@ServerEndpoint("/groupWebSocket")
@Component
public class GroupWebSocket {
    private Session session;

    private Long userId;

    public Session getSession() {
        return this.session;
    }

    private static GroupMessageMapper groupMessageMapper;

    private static UserGroupRelationMapper relationMapper;

    private static StringRedisTemplate stringRedisTemplate;

    @Autowired
    public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
        GroupWebSocket.stringRedisTemplate = stringRedisTemplate;
    }

    @Autowired
    public void setRelationMapper(UserGroupRelationMapper relationMapper) {
        GroupWebSocket.relationMapper = relationMapper;
    }

    @Autowired
    public void setGroupMessageMapper(GroupMessageMapper groupMessageMapper) {
        GroupWebSocket.groupMessageMapper = groupMessageMapper;
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        Gson g = new Gson();
        Map<String, Object> messageMap = g.fromJson(message, Map.class);
        // 如果长度为2则说明此信息为前端刚建立webSocket连接时发过来的信息, 进行储存用户session的操作
        if (messageMap.size() == 2) {
            // 将用户的id与session绑定存储
            this.session = session;
            this.userId = Long.valueOf(messageMap.get("userId") + "");
            ChatMessageContainer.userGroupMap.put(userId, this);
        } else {
            // 用户发送了群信息过来, 准备用websocket将群消息推送给群成员
            // 将信息转换成实体类对象
            GroupMessage groupMessage = new GroupMessage();
            groupMessage.setMessageContent(messageMap.get("content") + "");
            groupMessage.setMessageOwnerId(Long.valueOf(messageMap.get("userId") + ""));
            groupMessage.setMessageGroupId(Long.valueOf(messageMap.get("groupId") + ""));
            groupMessage.setMessageCreateTime(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()));
            groupMessageMapper.insert(groupMessage);
            // 处理要推送的群消息
            processMessageData(messageMap, groupMessage);
            // 查询群里面的所有用户id, 如果用户的session存在ChatMessageContainer.userGroupMap中则推送到该用户的客户端上
            List<Long> idList = relationMapper.selectAllUserByGroupId(Long.valueOf(messageMap.get("groupId") + ""));
            for (Long id : idList) {
                // 发信息的本人不需要进行推送的操作
                if (id.equals(Long.valueOf(messageMap.get("userId") + ""))) {
                    continue;
                }
                // 如果用户session存在, 将信息推送到客户端
                if (ChatMessageContainer.userGroupMap.get(id) != null) {
                    Session userSession = ChatMessageContainer.userGroupMap.get(id).getSession();
                    userSession.getBasicRemote().sendText(g.toJson(messageMap));
                }
                // TODO: 不存在可以做储存用户未读信息的功能
            }
        }
    }

    @OnClose
    public void onClose(Session session) {
        // 清除用户的session信息
        ChatMessageContainer.userGroupMap.remove(userId);
        System.out.println("主页面开始关闭");
    }

    private void processMessageData (Map<String, Object> messageMap, GroupMessage groupMessage) {
        Long userId = groupMessage.getMessageOwnerId();
        HashOperations<String, Object, Object> opsForHash = stringRedisTemplate.opsForHash();
        messageMap.put("createTime", groupMessage.getMessageCreateTime());
        messageMap.put("media", groupMessage.getMessageMedia());
        messageMap.put("id", groupMessage.getId());
        messageMap.put("nickname", opsForHash.get("user_" + userId, "nickname"));
        messageMap.put("avatar", "http://localhost:8888/static/upload/" + opsForHash.get("user_" + userId, "avatar"));
    }
}
