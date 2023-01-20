package com.friendship.service.impl;

import com.friendship.mapper.FriendlyRelationshipMapper;
import com.friendship.pojo.User;
import com.friendship.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@SuppressWarnings("all")
public class PersonalInfoService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FriendlyRelationshipMapper friendMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 通过token来得到相应的用户头像信息和id信息
     * @param token: 传过来的token
     * @return: 返回封装了头像和id信息的对象
     */
    public Map<String, Object> getAvatar(String token) {
        ValueOperations<String, String> redis = stringRedisTemplate.opsForValue();
        HashOperations<String, Object, Object> hashOperations = stringRedisTemplate.opsForHash();
        Map<String, Object> map = new HashMap<>();
        //先去redis中通过token得到id,再拿id在redis中得到头像的地址
        String id = redis.get(token);
        String avatar = (String) hashOperations.get("user_" + id, "avatar");
        map.put("id", id);
        map.put("avatar", avatar);
        //返回结果信息
        return map;
    }

    /**
     * 更新用户的头像信息
     * @param avatar: 传过来的base64类型的数据
     * @param token: 传过来的token
     * @return: 返回是否更新成功的信息
     * @throws Exception
     */
    public int uploadAvatar(String avatar, String token) throws Exception {
        ValueOperations<String, String> redis = stringRedisTemplate.opsForValue();
        //则先去redis中通过token得到id,再拿id去存放头像的地址
        String id = redis.get(token);
        //对base64数据进行解码
        Base64.Decoder decoder = Base64.getDecoder();
        //得到解码后的数据
        byte[] decode = decoder.decode(avatar.substring(22));
        //得到文件的上传地址
        String path = ResourceUtils.toURI(ResourceUtils.getURL("classpath:")).getPath() + "static/upload";
        //打包成jar包后, 要将保存的上传目录路径改为这个
//        String path = System.getProperty("user.dir") + "/static/upload";
        //以日期和小时数来创建文件夹,以此来应对一个文件夹内图片过多的问题
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd/HH");
        String format = simpleDateFormat.format(date);
        //创建相应的文件夹
        File filePath = new File(path + "/" + format);
        if (!filePath.exists()) {
            filePath.mkdirs();
        }
        //将图片存取到文件夹中
        String s = UUID.randomUUID().toString();
        FileOutputStream fileOutputStream = new FileOutputStream(path + "/" + format + "/" + s + ".png");
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
        bufferedOutputStream.write(decode);
        bufferedOutputStream.close();
        fileOutputStream.close();
        //将头像的地址信息保存到数据库中
        int i = userMapper.setAvatar(Long.valueOf(id), format + "/" + s + ".png");
        //更新redis中头像的地址信息
        stringRedisTemplate.opsForHash().put("user_" + id, "avatar", format + "/" + s + ".png");
        return i;
    }

    /**
     * 用户登录时访问个人信息页面时得到的用户信息
     * @param token: 传过来的token
     * @return: 返回包含本人基本信息的数据
     */
    public Map<Object, Object> getUserInfo(String token) {
        ValueOperations<String, String> redis = stringRedisTemplate.opsForValue();
        //则先去redis中通过token得到id,再拿id在redis中得到个人数据
        String id = redis.get(token);
        //返回结果信息
        return stringRedisTemplate.opsForHash().entries("user_" + id);
    }

    /**
     * 用户访问别人或者自己主页时看到的用户信息
     * @param id: 被访问者的用户id
     * @return: 返回被访问者的基本信息
     */
    public Map<Object, Object> getUserInfoById(String id) {
        Map<Object, Object> allInfoById = stringRedisTemplate.opsForHash().entries("user_" + id);
        Long userId = Long.valueOf(id);
        int allFollowNumber = friendMapper.getAllFollowNumber(userId);
        int allFansNumber = friendMapper.getAllFansNumber(userId);
        allInfoById.put("followNumber", allFollowNumber);
        allInfoById.put("fansNumber", allFansNumber);
        return allInfoById;
    }

    /**
     * 更新用户的基本信息
     * @param token: 传过来的token
     * @param user: 用户更新的数据
     * @return: 返回是否更新成功
     */
    public int updateUserInfo(String token, User user) {
        ValueOperations<String, String> redis = stringRedisTemplate.opsForValue();
        //则先去redis中通过token得到id,再拿id在数据库中更新个人数据
        String id = redis.get(token);
        user.setId(Long.valueOf(id));
        int i = userMapper.UpdateUserInfoById(user);
        Map<String, Object> allInfoById = userMapper.getAllInfoById(Long.valueOf(id));
        allInfoById.put("id", allInfoById.get("id") + "");
        stringRedisTemplate.opsForHash().putAll("user_" + id, allInfoById);
        return i;
    }

    /**
     * 根据条件搜索查询符合的用户
     * @param condition
     */
    public List<Map<String, Object>> searchUserByCondition(String condition){
        List<Map<String, Object>> userByCondition = userMapper.getUserByCondition(condition);
        List<Map<String, Object>> mapList = userByCondition.stream().map(p -> {
            p.put("id", "http://localhost:8082/u/" + p.get("id") + "/blog");
            p.put("avatar", "http://localhost:8888/static/upload/" + p.get("avatar"));
            return p;
        }).toList();
        return mapList;
    }
}
