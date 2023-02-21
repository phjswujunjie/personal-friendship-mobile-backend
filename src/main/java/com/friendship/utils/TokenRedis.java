package com.friendship.utils;

import com.friendship.mapper.UserMapper;
import com.friendship.pojo.User;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
public class TokenRedis {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private UserMapper userMapper;

    @Scheduled(cron = "0 30 0 * * *")
    public void updateUserInfoByRegular(){
        System.out.println("定时任务执行了......");
        ListOperations<String, String> stringStringListOperations = stringRedisTemplate.opsForList();
        HashOperations<String, Object, Object> opsForHash = stringRedisTemplate.opsForHash();
        Long idList = stringStringListOperations.size("id_list");
        List<String> userId = stringStringListOperations.range("id_list", 0, idList);
        Gson g = new Gson();
        for (String id : userId) {
            Map<Object, Object> user = opsForHash.entries("user_" + id);
            User user1 = g.fromJson(g.toJson(user), User.class);
            userMapper.updateById(user1);
        }
    }

    public static void tokenToRedis(StringRedisTemplate redis, String token, String id, Map<String, Object> userInfo) {
        //登录或者注册的话就将生成的token和account存入redis,以此来保持用户的登录状态(为什么要存放两个相反的键值对?用来实现单点登录,当同一个账号
        // 在主机1登录时会生成token1,并且将account和token1存放到redis中, 当在主机2登录时会生成token2,此时account对应的token就会从token1
        // 变成token2,从而使主机1的token失效(看loginOrOut工具类判断是否登录的源代码),登录状态解除)
        ValueOperations<String, String> stringRedis = redis.opsForValue();
        HashOperations<String, Object, Object> hashRedis = redis.opsForHash();
        stringRedis.set(token, id, 7 * 24 * 60 * 60, TimeUnit.SECONDS);
        stringRedis.set(id, token, 7 * 24 * 60 * 60, TimeUnit.SECONDS);
        if (userInfo != null){
            hashRedis.putAll("user_" + id, userInfo);
        }
    }

    /**
     * 根据token来判断是否登录
     * @param redis
     * @param token
     * @return
     */
    public static Boolean hasLogin(StringRedisTemplate redis, String token){
        //如果发过来的token为null,则直接返回没有登录的状态信息
        if (token == null){
           return false;
        }
        //通过token得到id
        ValueOperations<String, String> stringRedis = redis.opsForValue();
        String id = stringRedis.get(token);
        //通过id不为null的话,则开始判断id对应的token是否与用户发过来的token相同,相同的话则说明已经登录,不相同则说明该账号为多点登录
        // ,保留最新的登录状态,解除其它的登录状态,从而实现单点登录,
        if (id != null){
            String dbToken = stringRedis.get(id);
            if (Objects.equals(dbToken, token)){
                //如果返回结果显示已经登录,则将redis中的登录状态时间重新刷新
                //这里的重新设置状态时间操作所用的值全为redis中的值,从而避免了对数据库读取的操作
                TokenRedis.tokenToRedis(redis, token, id, null);
                return true;
            }
        }
        //如果account不存在或者account对应的token与用户发送过来的token不一样,则直接返回没有登录的状态信息
        return false;
    }

    @SuppressWarnings("all")
    public static boolean isSelf(ValueOperations<String, String> redis, String token, Long id){
        return id.equals(Long.valueOf(redis.get(token)));
    }
}
