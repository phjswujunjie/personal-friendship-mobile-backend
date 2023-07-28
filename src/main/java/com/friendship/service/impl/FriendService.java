package com.friendship.service.impl;

import com.friendship.mapper.ChatMessageMapper;
import com.friendship.mapper.FriendlyRelationshipMapper;
import com.friendship.mapper.GroupMessageMapper;
import com.friendship.mapper.UserMapper;
import com.friendship.pojo.FriendlyRelationship;
import com.friendship.pojo.User;
import com.friendship.utils.CommonString;
import com.friendship.utils.TokenRedis;
import com.friendship.websocket.ChatMessageContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class FriendService {
    @Resource
    private FriendlyRelationshipMapper friendMapper;
    @Resource
    private ChatMessageMapper chatMessageMapper;
    @Resource
    private UserGroupRelationService groupRelationService;
    @Resource
    private GroupMessageMapper groupMessageMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 返回用户的最新信息和未读信息的数量
     *
     * @param idList
     * @param userId
     * @return
     */
    public List<Map> queryStatus(Long[] idList, Long userId) {
        Map<Long, Boolean> map = new HashMap<>();
        Map<Long, Integer> messageNumberMap = new HashMap<>();
        Map<Long, Object> messageMap = new HashMap<>();
        for (Long id : idList) {
            map.put(id, ChatMessageContainer.userMap.containsKey(id));
        }
        Map<Long, List<Map<String, Object>>> messageListMap = ChatMessageContainer.messageMap.get(userId);
        for (Long id : idList) {
            if (messageListMap != null && messageListMap.get(id) != null) {
                if (messageListMap.get(id).size() != 0) {
                    messageNumberMap.put(id, messageListMap.get(id).size());
                    messageMap.put(id, messageListMap.get(id).get(messageListMap.get(id).size() - 1).get("content"));
                }
            } else {
                messageNumberMap.put(id, 0);
            }
        }
        List<Map> mapList = new ArrayList<>();
        // 再查询群聊的未读信息和最新的信息
        Map<Long, Integer> groupMessageNumber = new HashMap<>();
        Map<Long, String> groupLatestMessage = new HashMap<>();
        Map<Long, Map<Long, AtomicInteger>> groupMessageNumberMap = ChatMessageContainer.groupMessageNumberMap;
        if (groupMessageNumberMap.get(userId) != null) {
            // 得到相应群聊的未读信息数量
            Map<Long, AtomicInteger> atomicIntegerMap = groupMessageNumberMap.get(userId);
            for (Long l: atomicIntegerMap.keySet()) {
                int i = atomicIntegerMap.get(l).get();
                // 如果未读信息数量不为0, 则再查询最新的一条信息
                if (i != 0) {
                    String message = groupMessageMapper.selectLatestGroupMessage(l);
                    groupLatestMessage.put(l, message);
                    groupMessageNumber.put(l, i);
                }
            }
        }
        mapList.add(map);
        mapList.add(messageNumberMap);
        mapList.add(messageMap);
        mapList.add(groupMessageNumber);
        mapList.add(groupLatestMessage);
        return mapList;
    }

    /**
     * 增加关注关系
     *
     * @param token:    传过来的token
     * @param followId: 关注对象的id
     * @return
     */
    public int addFriend(String token, Long followId) {
        Long userId = Long.valueOf(Objects.requireNonNull(stringRedisTemplate.opsForValue().get(token)));
        String format = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDate.now());
        FriendlyRelationship friend = new FriendlyRelationship(userId, followId, format);
        int result = friendMapper.relationshipIsExist(userId, followId);
        if (result == 1) {
            return friendMapper.addFriendlyRelationship(userId, followId);
        } else {
            return friendMapper.insert(friend);
        }
    }

    /**
     * 查询两个用户之间的关系
     *
     * @param token:         传过来的token(可得到本人id)
     * @param anotherUserId: 查询的另一个用户的id
     * @return: 返回两个用户之间的关系(2代表好友关系, 0代表没有关系, 4代表了访问者关注了被访问者, 8代表被访问者为访问者的粉丝)
     */
    public int queryRelation(String token, Long anotherUserId) {
        if (token != null) {
            if (TokenRedis.hasLogin(stringRedisTemplate, token)) {
                return -1;
            }
            Long userId = Long.valueOf(Objects.requireNonNull(stringRedisTemplate.opsForValue().get(token)));
            List<Map<String, Object>> mapList = friendMapper.queryRelation(userId, anotherUserId);
            //如果结果数量为0则代表没有关系, 为2则代表为朋友关系
            if (mapList.size() == 0) {
                return 50000;
            } else if (mapList.size() == 2) {
                return 50003;
            }
            //为1则代表两个人之间有关注关系(但是不知道谁关注了谁)
            Map<String, Object> map = mapList.get(0);
            Long user_id = Long.valueOf(map.get("userId") + "");
            if (user_id.equals(userId)) {
                //访问者关注了被访问者
                return 50002;
            }
            //被访问者为访问者的粉丝
            return 50001;
        } else {
            return -1;
        }
    }

    /**
     * 删除两个好友间的单向关系
     *
     * @param token:    传过来的token(包含进行删除操作的用户的id)
     * @param followId: 进行删除操作的用户想删除的好友的id
     * @return
     */
    public int deleteFriend(String token, Long followId) {
        Long userId = Long.valueOf(Objects.requireNonNull(stringRedisTemplate.opsForValue().get(token)));
        return friendMapper.deleteFriend(userId, followId);
    }

    /**
     * 得到一个用户所有的关注或者粉丝信息
     *
     * @param token: 传过来的token
     * @param flag:  想要进行何种操作的标志信息(查询关注为1, 查询粉丝为2)
     * @return: 返回相应的用户数据
     */
    public List<List<Map<String, Object>>> getAllFollowOrFansInfo(String token, int flag) {
        List<List<Map<String, Object>>> list = new LinkedList<>();
        List<Map<String, Object>> listFollowOrFans = new LinkedList<>();
        List<Map<String, Object>> listFriend = new LinkedList<>();
        List<Long> allFollowInfo;
        Long userId = Long.valueOf(Objects.requireNonNull(stringRedisTemplate.opsForValue().get(token)));
        Integer relation;
        if (flag == 1) {
            allFollowInfo = friendMapper.getFollow(userId);
            relation = 50002;
        } else {
            allFollowInfo = friendMapper.getFans(userId);
            relation = 50001;
        }
        processUserInfo(listFollowOrFans, allFollowInfo, relation);
        List<Long> allFriendInfo = friendMapper.getFriends(userId);
        processUserInfo(listFriend, allFriendInfo, 50003);
        list.add(listFollowOrFans);
        list.add(listFriend);
        return list;
    }

    /**
     * 得到用户的全部好友和群组信息
     *
     * @param token: 传过来的token
     * @return: 返回用户的全部好友的信息
     */
    public List<List<Map<String, Object>>> getAllFriendInfo(String token) {
        List<Map<String, Object>> listFriend = new LinkedList<>();
        Long userId = Long.valueOf(Objects.requireNonNull(stringRedisTemplate.opsForValue().get(token)));
        List<Long> allFriendInfo = friendMapper.getFriends(userId);
        processUserInfo(listFriend, allFriendInfo, 50003, userId);
        List<Map<String, Object>> groupByUserId = groupRelationService.getGroupByUserId(token);
        List<List<Map<String, Object>>> list = new ArrayList<>();
        list.add(listFriend);
        list.add(groupByUserId);
        return list;
    }

    /**
     * 用来处理从数据库中查出来的用户信息
     *
     * @param listFriend:    包含全部用户信息的集合对象
     * @param allFriendInfo: 包含全部用户id信息的集合对象(用id来查到相应的用户信息)
     * @param userId         : 如果传递了此形参则代表还要查询用户的未读信息和最新的一条信息记录, 该参数为当前用户的id
     */
    private void processUserInfo(List<Map<String, Object>> listFriend, List<Long> allFriendInfo, Integer relation, Long... userId) {
        if (allFriendInfo.size() != 0) {
            Map<Long, List<Map<String, Object>>> messageListMap = null;
            List<User> users = userMapper.selectBatchIds(allFriendInfo);
            if (userId.length != 0) {
               messageListMap = ChatMessageContainer.messageMap.get(userId[0]);
            }
            for (User user : users) {
                Map<String, Object> map = new HashMap<>();
                map.put("homepageLocation", CommonString.FRONTEND_ADDRESS + "u/" + user.getId() + "/blog");
                map.put("id", user.getId());
                map.put("avatar", CommonString.RESOURCES_ADDRESS + user.getAvatar());
                map.put("nickname", user.getNickname());
                map.put("introduction", user.getIntroduction());
                map.put("relation", relation);
                // 查询与该用户的未读信息和最新的一条信息记录
                if (userId.length != 0) {
                    // 查询最新的一条信息
                    String message = chatMessageMapper.getLatestMessage(userId[0], user.getId());
                    map.put("message", message == null ? "" : message);
                    // 再查询未读的信息数量
                    Long id = user.getId();
                    if (messageListMap != null && messageListMap.get(id) != null) {
                        map.put("unreadMessageNumber", messageListMap.get(id).size());
                    } else {
                        map.put("unreadMessageNumber", 0);
                    }
                }
                listFriend.add(map);
            }
        }
    }
}
