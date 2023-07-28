package com.friendship.service.impl;

import com.friendship.mapper.UserGroupMapper;
import com.friendship.mapper.UserGroupRelationMapper;
import com.friendship.mapper.UserMapper;
import com.friendship.pojo.User;
import com.friendship.pojo.UserGroup;
import com.friendship.pojo.UserGroupRelation;
import com.friendship.utils.CommonString;
import com.friendship.utils.MD5;
import com.friendship.utils.TokenRedis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class GroupService {
    @Resource
    private UserGroupMapper userGroupMapper;

    @Resource
    private UserGroupRelationMapper relationMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private UserMapper userMapper;

    /**
     * 创建新的群聊
     *
     * @param userGroup: 传过来的要新建的群聊对象
     * @param token:     token
     * @param avatar:    群头像的文件对象
     * @return
     * @throws Exception
     */
    public Long createGroup(UserGroup userGroup, String token, MultipartFile avatar) throws Exception {
        Long userId = Long.valueOf(stringRedisTemplate.opsForValue().get(token));
        String groupId = MD5.getGroupId();
        // 储存群聊的头像信息
        String realPath = ResourceUtils.toURI(ResourceUtils.getURL("classpath:")).getPath();
        String path = DateTimeFormatter.ofPattern("yyyy/MM/dd/").format(LocalDateTime.now());
        String suffix = avatar.getOriginalFilename().substring(avatar.getOriginalFilename().lastIndexOf("."));
        String fileName = UUID.randomUUID() + suffix;
        // 利用另一个线程去执行耗时的操作
        new Thread(() -> {
            File directory = new File(realPath + "/static/upload/" + path);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            try {
                avatar.transferTo(new File(directory.getPath() + "/" + fileName));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
        String image = path + fileName;
        // 创建群
        String createTime = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDate.now());
        userGroup.setGroupOwnerId(userId);
        userGroup.setGroupCreateTime(createTime);
        userGroup.setGroupId(groupId);
        userGroup.setGroupAvatar(image);
        userGroup.setGroupMemberNumber(1);
        userGroupMapper.insert(userGroup);
        // 创建用户与该群的关系
        Long id = userGroup.getId();
        UserGroupRelation relation = new UserGroupRelation();
        relation.setStatus(1);
        relation.setHasView(1);
        relation.setUserId(userId);
        relation.setGroupId(id);
        relation.setCreateTime(createTime);
        relationMapper.insert(relation);
        return userGroup.getId();
    }

    /**
     * 得到群聊设置的相关信息, 登录才能查看
     * @param groupId
     * @param token
     * @return
     */
    public List<Map<String, Object>> getGroupSettingInfoById(Long groupId, String token) {
        // 得到自己id
        Long userId = Long.valueOf(stringRedisTemplate.opsForValue().get(token));
        // 根据自己的id去获取到自己对群组的备注信息以及群昵称
        List<UserGroupRelation> relations = relationMapper.selectRelationByUserIdAndGroupId(userId, groupId);
        // 如果为0, 则代表用户并没有加入该群聊
        if (relations.size() == 0) {
            // 为非法请求, 直接返回null
            return null;
        }
        // 加入群聊了才能查看群聊设置
        List<Map<String, Object>> list = new ArrayList<>();
        // 得到群组对象
        UserGroup userGroup = userGroupMapper.selectById(groupId);
        // 将群组对象转换成Map对象
        Map<String, Object> groupMap = processUserGroupToMap(userGroup);
        // 得到群主id
        Long groupLeader = Long.valueOf(groupMap.get("groupOwnerId") + "");
        // TODO:得到所有管理员的id, 可以进行相关的操作
        // String groupManagerId = userGroup.getGroupManagerId();
        // 查询用户本人是否为群聊的群主
        if (groupLeader.equals(userId)) {
            groupMap.put("isGroupLeader", true);
        } else {
            groupMap.put("isGroupLeader", false);
        }
        // 将群的备注名和自己在群中的昵称不为''则加入Map中
        UserGroupRelation relation = relations.get(0);
        groupMap.put("selfNickname", relation.getGroupNickname());
        groupMap.put("groupNote", relation.getGroupNote());
        // 进群时间
        groupMap.put("enterGroupTime", relation.getCreateTime());
        // 是否开启置顶
        groupMap.put("isTop", relation.getIsTop());
        list.add(groupMap);
        // 查询群聊中前几个的用户信息, 便于在前端进行展示
        List<Long> idList = relationMapper.selectAllUserByGroupIdByPage(groupId, 0L, 4L);
        HashOperations<String, Object, Object> opsForHash = stringRedisTemplate.opsForHash();
        for (Long id : idList) {
            Map<String, Object> map = new HashMap<>();
            map.put("userId", id);
            map.put("avatar", CommonString.RESOURCES_ADDRESS + userMapper.getAllInfoById(id).get("avatar"));
            map.put("nickname", userMapper.getAllInfoById(id).get("nickname"));
            list.add(map);
        }
        return list;
    }

    /**
     * 得到展示群聊主页的相关信息, 不需要登录就可以查看
     * @param groupId
     * @param token
     * @return
     */
    public List<Map<String, Object>> getGroupHomepageInfoById(Long groupId, String token) {
        List<Map<String, Object>> list = new ArrayList<>();
        // 得到群组对象
        UserGroup userGroup = userGroupMapper.selectById(groupId);
        // 将群组对象转换成Map对象
        Map<String, Object> groupMap = processUserGroupToMap(userGroup);
        list.add(groupMap);
        Boolean loginStatus = TokenRedis.hasLogin(stringRedisTemplate, token);
        groupMap.put("loginStatus", loginStatus);
        // 如果登录了才查询自己是否加入群聊
        if (loginStatus) {
            // 得到自己id
            Long userId = Long.valueOf(stringRedisTemplate.opsForValue().get(token));
            // 根据自己的id去获取到自己对群组的备注信息以及群昵称
            List<UserGroupRelation> relations = relationMapper.selectRelationByUserIdAndGroupId(userId, groupId);
            // 如果为0, 则代表用户并没有加入该群聊
            if (relations.size() == 0) {
                // 将未加入群聊的信息放到Map容器中, 直接进行返回
                groupMap.put("isGroupMember", false);
            } else {
                // 不为0, 则代表用户加入了群聊
                groupMap.put("isGroupMember", true);
            }
        }
        return list;
    }

    // 处理群组的信息, 将群组对象转换到Map集合
    private Map<String, Object> processUserGroupToMap(UserGroup userGroup) {
        // 创建一个Map容器
        Map<String, Object> groupMap = new HashMap<>();
        groupMap.put("id", userGroup.getId());
        groupMap.put("groupName", userGroup.getGroupName());
        // 群号
        groupMap.put("groupId", userGroup.getGroupId());
        groupMap.put("groupAvatar", CommonString.RESOURCES_ADDRESS + userGroup.getGroupAvatar());
        groupMap.put("groupIntroduction", userGroup.getGroupIntroduction());
        groupMap.put("groupNotice", userGroup.getGroupNotice());
        groupMap.put("groupMemberNumber", userGroup.getGroupMemberNumber());
        groupMap.put("groupCreateTime", userGroup.getGroupCreateTime());
        groupMap.put("groupOwnerId", userGroup.getGroupOwnerId());
        groupMap.put("groupCapacity", userGroup.getGroupCapacity());
        return groupMap;
    }
}
