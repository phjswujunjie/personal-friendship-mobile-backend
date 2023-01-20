package com.friendship.service.impl;

import com.friendship.mapper.FriendlyRelationshipMapper;
import com.friendship.mapper.UserMapper;
import com.friendship.pojo.FriendlyRelationship;
import com.friendship.pojo.User;
import com.friendship.utils.TokenRedis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
@SuppressWarnings("all")
public class FriendService {
    @Autowired
    private FriendlyRelationshipMapper friendMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 增加关注关系
     * @param token: 传过来的token
     * @param followId: 关注对象的id
     * @return
     */
    public int addFriend(String token, Long followId) {
        Long userId = Long.valueOf(Objects.requireNonNull(stringRedisTemplate.opsForValue().get(token)));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String format = simpleDateFormat.format(new Date());
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
     * @param token: 传过来的token(可得到本人id)
     * @param anotherUserId: 查询的另一个用户的id
     * @return: 返回两个用户之间的关系(2代表好友关系, 0代表没有关系, 4代表了访问者关注了被访问者, 8代表被访问者为访问者的粉丝)
     */
    public int queryRelation(String token, Long anotherUserId) {
        if (token != null){
            Map<String, Object> map1 = TokenRedis.hasLogin(stringRedisTemplate, token);
            if (!(boolean)map1.get("loginStatus")){
                return -1;
            }
            Long userId = Long.valueOf(Objects.requireNonNull(stringRedisTemplate.opsForValue().get(token)));
            List<Map<String, Object>> mapList = friendMapper.queryRelation(userId, anotherUserId);
            //如果结果数量为0则代表没有关系, 为2则代表为朋友关系
            if (mapList.size() != 1) {
                return mapList.size();
            }
            //为1则代表两个人之间有关注关系(但是不知道谁关注了谁)
            Map<String, Object> map = mapList.get(0);
            Long user_id = Long.valueOf(map.get("userId") + "");
            if (user_id.equals(userId)) {
                //访问者关注了被访问者
                return 4;
            }
            //被访问者为访问者的粉丝
            return 8;
        }else {
            return -1;
        }
    }

    /**
     * 删除两个好友间的单向关系
     * @param token: 传过来的token(包含进行删除操作的用户的id)
     * @param followId: 进行删除操作的用户想删除的好友的id
     * @return
     */
    public int deleteFriend(String token, Long followId) {
        Long userId = Long.valueOf(Objects.requireNonNull(stringRedisTemplate.opsForValue().get(token)));
        return friendMapper.deleteFriend(userId, followId);
    }

    /**
     * 得到一个用户所有的关注或者粉丝信息
     * @param token: 传过来的token
     * @param flag: 想要进行何种操作的标志信息(查询关注为1, 查询粉丝为2)
     * @return: 返回相应的用户数据
     */
    public List<List<Map<String, Object>>> getAllFollowOrFansInfo(String token, int flag) {
        List<List<Map<String, Object>>> list = new LinkedList<>();
        List<Map<String, Object>> listFollowOrFans = new LinkedList<>();
        List<Map<String, Object>> listFriend = new LinkedList<>();
        List<Long> allFollowInfo;
        Long userId = Long.valueOf(Objects.requireNonNull(stringRedisTemplate.opsForValue().get(token)));
        if (flag == 1) {
            allFollowInfo = friendMapper.getFollow(userId);
        } else {
            allFollowInfo = friendMapper.getFans(userId);
        }
        processUserInfo(listFollowOrFans, allFollowInfo);
        List<Long> allFriendInfo = friendMapper.getFriends(userId);
        processUserInfo(listFriend, allFriendInfo);
        list.add(listFollowOrFans);
        list.add(listFriend);
        return list;
    }

    /**
     * 得到用户的全部好友
     * @param token: 传过来的token
     * @return: 返回用户的全部好友的信息
     */
    public List<Map<String, Object>> getAllFriendInfo(String token) {
        List<Map<String, Object>> listFriend = new LinkedList<>();
        Long userId = Long.valueOf(Objects.requireNonNull(stringRedisTemplate.opsForValue().get(token)));
        List<Long> allFriendInfo = friendMapper.getFriends(userId);
        processUserInfo(listFriend, allFriendInfo);
        return listFriend;
    }

    /**
     * 用来处理从数据库中查出来的用户信息
     * @param listFriend: 包含全部用户信息的集合对象
     * @param allFriendInfo: 包含全部用户id信息的集合对象(用id来查到相应的用户信息)
     */
    private void processUserInfo(List<Map<String, Object>> listFriend, List<Long> allFriendInfo) {
        if (allFriendInfo.size() != 0) {
            List<User> users = userMapper.selectBatchIds(allFriendInfo);
            for (User user : users) {
                Map<String, Object> map = new HashMap<>();
                map.put("homepageLocation", "http://localhost:8081/u/" + user.getId() + "/blog");
                map.put("id", user.getId());
                map.put("avatar", "http://localhost:8888/static/upload/" + user.getAvatar());
                map.put("nickname", user.getNickname());
                map.put("introduction", user.getIntroduction());
                listFriend.add(map);
            }
        }
    }
}
