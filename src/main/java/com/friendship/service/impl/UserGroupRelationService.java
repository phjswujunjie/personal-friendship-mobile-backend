package com.friendship.service.impl;

import com.friendship.mapper.UserGroupMapper;
import com.friendship.mapper.UserGroupRelationMapper;
import com.friendship.pojo.UserGroup;
import com.friendship.pojo.UserGroupRelation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class UserGroupRelationService {
    @Autowired
    private UserGroupRelationMapper groupRelationMapper;

    @Autowired
    private UserGroupMapper userGroupMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public Integer exitGroupChat(Long groupId, String token) {
        Long userId = Long.valueOf(stringRedisTemplate.opsForValue().get(token));
        Integer integer = groupRelationMapper.deleteUserGroupRelation(groupId, userId);
        // 如果删除成功了, 则将群的群员数量减一
        Integer res = null;
        if (integer != null) {
            res = userGroupMapper.updateUserGroupMemberNumberById(groupId, -1);
        }
        return res;
    }

    public Long getUnreadEnterGroupMessageNumber(String token) {
        Long leaderId = Long.valueOf(stringRedisTemplate.opsForValue().get(token));
        // 查询群组的群主为leaderId的群, 从而得到申请信息
        List<UserGroup> groups = userGroupMapper.selectGroupByGroupLeaderId(leaderId);
        AtomicReference<Long> messageNumber = new AtomicReference<>(0L);
        //
        groups.forEach(p -> {
            messageNumber.updateAndGet(v -> v + groupRelationMapper.selectUnreadMessageNumberByGroupId(p.getId()));
        });
        return messageNumber.get();
    }

    public List<Map<String, Object>> getAllApplyByGroupLeaderId(String token) {
        List<Map<String, Object>> applyList = new ArrayList<>();
        HashOperations<String, Object, Object> opsForHash = stringRedisTemplate.opsForHash();
        // 得到群主的id
        Long groupLeaderId = Long.valueOf(stringRedisTemplate.opsForValue().get(token));
        // 查询群组的群主为groupLeaderId的群, 从而得到申请信息
        List<UserGroup> groups = userGroupMapper.selectGroupByGroupLeaderId(groupLeaderId);
        groups.forEach(p -> {
            // 得到所有申请加入该群的用户id
            List<Long> userIdList = groupRelationMapper.selectAllApplyByGroupId(p.getId());
            // 再将所有申请加入该群的关系信息的has_view设置未1, 代表该条信息已经被群主查看
            groupRelationMapper.updateHasViewField(p.getId());
            userIdList.forEach(u -> {
                Map<String, Object> map = new HashMap<>();
                map.put("userId", u);
                map.put("userNickname", opsForHash.get("user_" + u, "nickname"));
                map.put("userAvatar", "http://localhost:8888/static/upload/" + opsForHash.get("user_" + u, "avatar"));
                map.put("groupId", p.getId());
                map.put("groupName", p.getGroupName());
                map.put("groupAvatar", p.getGroupAvatar());
                applyList.add(map);
            });
        });
        return applyList;
    }

    /**
     * 申请加入群聊, status为0, 只有当群主审核通过才可以将status设为1, 正式成为成员
     *
     * @param groupId: 群id
     * @param token
     * @return
     */
    public Integer applyToJoinGroup(Long groupId, String token) {
        // 当用户申请群后, 先去查询数据库中是否有相关的申请记录或者以前进入过该群(将is_delete变为0即可)
        Long userId = Long.valueOf(stringRedisTemplate.opsForValue().get(token));
        // 如果以前申请过, 则更新创建时间即可
        Boolean applyGroupBefore = groupRelationMapper.isApplyOrEnterGroupOrBefore(userId, groupId, 0L);
        // 再查询以前是否进入过该群
        Boolean enterGroupBefore = groupRelationMapper.isApplyOrEnterGroupOrBefore(userId, groupId, 1L);
        // 如果以前申请过或者进入过该群, 则更新创建时间和将isDelete改为0即可
        String createTime = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDate.now());
        if (applyGroupBefore || enterGroupBefore) {
            return groupRelationMapper.updateRelationCreateTimeAndIsDelete(userId, groupId, createTime);
        }
        // 没有申请或者进入过, 则新建一条记录
        UserGroupRelation entity = new UserGroupRelation();
        entity.setGroupId(groupId);
        entity.setUserId(userId);
        entity.setStatus(0);
        entity.setHasView(0);
        entity.setCreateTime(createTime);
        return groupRelationMapper.insert(entity);
    }

    /**
     * 同意加入群聊
     *
     * @param token
     * @param userId
     * @param groupId
     * @return
     */
    public Integer allowUserJoinGroup(String token, Long userId, Long groupId) {
        Long groupLeaderId = Long.valueOf(stringRedisTemplate.opsForValue().get(token));
        if (userId == null || groupId == null) {
            return null;
        }
        // 先根据群的id查询到对应的UserGroup对象, 查询群主是否为groupLeaderId本人
        UserGroup userGroup = userGroupMapper.selectById(groupId);
        if (userGroup == null) {
            return null;
        }
        // 如果群主不是groupLeaderId本人, 则返回null, 为非法请求
        if (!groupLeaderId.equals(userGroup.getGroupOwnerId())) {
            return null;
        }
        // 将群的人数加一
        userGroupMapper.updateUserGroupMemberNumberById(groupId, 1);
        // 是本人则将status改为1, 将userId正式成为groupId的群员
        return groupRelationMapper.updateRelationStatus(userId, groupId);
    }

    /**
     * 通过用户id得到参加的所有群信息
     *
     * @param token: 用户的token
     * @return
     */
    public List<Map<String, Object>> getGroupByUserId(String token) {
        Long userId = Long.valueOf(stringRedisTemplate.opsForValue().get(token));
        List<Map<String, Object>> list = groupRelationMapper.selectGroupByUserId(userId);
        return list.stream().map(p -> {
            UserGroup group = userGroupMapper.selectById((Long) p.get("groupId"));
            p.put("groupAvatar", group.getGroupAvatar());
            p.put("groupName", group.getGroupName());
            return p;
        }).collect(Collectors.toCollection(ArrayList::new));
    }
}
