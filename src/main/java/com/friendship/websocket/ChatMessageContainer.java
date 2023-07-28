package com.friendship.websocket;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

//存储用户在线状态和聊天信息的容器
public class ChatMessageContainer {

    //储存用户上线状态的Map容器
    public static final Map<Long, FriendshipWebSocket> userMap = new ConcurrentHashMap<>();

    //储存用户还没有查看的信息(未读信息)容器(一个用户(Map)接收到的所有其他所有用户(Map)的全部信息(Map, key为创建时间, value为信息))
    public static final Map<Long, Map<Long, List<Map<String, Object>>>> messageMap = new ConcurrentHashMap<>();

    // 储存用户群组Session的容器, 当用户加入任意一个群组聊天室, 则会储存用户相应的Session信息, 离开时则移除相对应的Session
    public static final Map<Long, GroupWebSocket> userGroupMap = new ConcurrentHashMap<>();

    // 储存用户为查看的群组信息的数量, 一个用户拥有一个容器, 一个容器中有用户所加入群组的全部未读信息数量
    public static final Map<Long, Map<Long, AtomicInteger>> groupMessageNumberMap = new ConcurrentHashMap<>();

    // 储存信息的确认id的容器
    public static final List<String> messageConfirmList = new ArrayList<>();
}
