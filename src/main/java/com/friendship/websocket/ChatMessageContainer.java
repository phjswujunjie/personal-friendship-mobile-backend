package com.friendship.websocket;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//存储用户在线状态和聊天信息的容器
public class ChatMessageContainer {

    //储存用户上线状态的Map容器
    public static Map<Long, FriendshipWebSocket> userMap = new ConcurrentHashMap<>();

    //储存用户还没有查看的信息(未读信息)容器(一个用户(Map)接收到的所有其他所有用户(Map)的全部信息(Map, key为创建时间, value为信息))
    public static Map<Long, Map<Long, List<Map<String, Object>>>> messageMap = new ConcurrentHashMap<>();
}
