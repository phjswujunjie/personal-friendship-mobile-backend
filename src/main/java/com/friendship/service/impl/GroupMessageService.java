package com.friendship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.friendship.mapper.GroupMessageMapper;
import com.friendship.mapper.UserGroupMapper;
import com.friendship.mapper.UserGroupRelationMapper;
import com.friendship.pojo.UserGroup;
import com.friendship.pojo.UserGroupRelation;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GroupMessageService {
    @Autowired
    private GroupMessageMapper groupMessageMapper;

    @Autowired
    private UserGroupMapper userGroupMapper;

    @Autowired
    private UserGroupRelationMapper userGroupRelationMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 根据群组id得到相关的信息
     * @param id: 群组id
     * @param request
     * @return
     */
    public List<Map<String, Object>> getGroupMessageByGroupId(Long id, HttpServletRequest request) {
        Long userId = Long.valueOf(stringRedisTemplate.opsForValue().get(request.getHeader("token")));
        // 在执行查询群信息时, 查询用户是否在该群内
        List<UserGroupRelation> relations = userGroupRelationMapper.selectRelationByUserIdAndGroupId(userId, id);
        // 如果查询结果为0, 则返回null(非法请求)
        if (relations.size() == 0) {
            return null;
        }
        // 查询结果长度不为0, 则意味着用户在群内, 则处理聊天信息
        List<Map<String, Object>> groupMessageList = groupMessageMapper.selectGroupMessageByGroupId(id);
        HashOperations<String, Object, Object> opsForHash = stringRedisTemplate.opsForHash();
        // 处理群聊信息
        ArrayList<Map<String, Object>> collect = groupMessageList.stream().map(p -> {
            p.put("avatar", "http://localhost:8888/static/upload/" + opsForHash.get("user_" + p.get("userId"), "avatar"));
            p.put("nickname", opsForHash.get("user_" + p.get("userId"), "nickname"));
            return p;
        }).collect(Collectors.toCollection(ArrayList::new));
        // 得到自己的头像,昵称,id
        Map<String, Object> map = new HashMap<>();
        map.put("selfId", userId);
        map.put("selfAvatar", "http://localhost:8888/static/upload/" + opsForHash.get("user_" + userId, "avatar"));
        // 再查询群名
        // 群的配置信息表
        UserGroup userGroup = userGroupMapper.selectById(id);
        // 得到用户-群组关系数据
        UserGroupRelation relation = relations.get(0);
        // 如果用户给该群备注了, 则将群名改为备注名, 否则即为群的真实名称
        map.put("groupName", ("").equals(relation.getGroupNote()) ? userGroup.getGroupName() : relation.getGroupNote());
        // 如果用户指定了在群中的昵称, 则将昵称改为在群主的昵称名, 否则即为自己的真实名称
        map.put("selfNickname", ("").equals(relation.getGroupNickname()) ? opsForHash.get("user_" + userId, "nickname") : relation.getGroupNickname());
        // 得到群聊的人数
        map.put("groupMemberNumber", userGroup.getGroupMemberNumber());
        collect.add(0, map);
        return collect;
    }
}
