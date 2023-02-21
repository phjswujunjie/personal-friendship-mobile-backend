package com.friendship.websocket;

//根据是否登录操作用户的上线和下线信息
import com.friendship.mapper.UserMapper;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.List;
import java.util.Map;

@ServerEndpoint("/loginInfoWebSocket")
@Component
@CrossOrigin(originPatterns = {"*"}, allowCredentials = "true")
public class SaveLoginInfoWebSocket {

    //为什么要用static?因为spring默认只会创建一个对象, 而websocket由于有多个客户端则会创建多个websocket对象, 所以应该让此对象被所有的websocket
    //对象共享
    private static UserMapper userMapper;

    private Long userId;

    @Autowired
    public void setUserMapper(UserMapper userMapper) {
        SaveLoginInfoWebSocket.userMapper = userMapper;
    }

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("连接建立成功!!!");
    }

    //收到信息触发
    @OnMessage
    public void onMessage(String message, Session session) throws Exception {
        Gson g = new Gson();
        userId = Long.valueOf( (String) g.fromJson(message, Map.class).get("userId"));
        //创建用户上线登录的信息
        if (userMapper.userIsNotExists(userId) == 1) {
            FriendshipWebSocket friendshipWebSocket = new FriendshipWebSocket();
            ChatMessageContainer.userMap.put(userId, friendshipWebSocket);
            System.out.println("userMap:" + ChatMessageContainer.userMap);
        }
//        将用户未查看的信息条数推送到客户端
        int messageNumber = 0;
        if (ChatMessageContainer.messageMap.get(userId) != null) {
            Map<Long, List<Map<String, Object>>> longListMap = ChatMessageContainer.messageMap.get(userId);
            for (List<Map<String, Object>> list : longListMap.values()) {
                messageNumber += list.size();
            }
            System.out.println("longListMap" + longListMap);
        }
        session.getBasicRemote().sendText(messageNumber + "");
    }

    //连接关闭触发
    @OnClose
    public void onClose(Session session) {
        ChatMessageContainer.userMap.remove(userId);
        System.out.println("主页面开始关闭");
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.out.println("连接异常");
        throwable.printStackTrace();
    }
}
