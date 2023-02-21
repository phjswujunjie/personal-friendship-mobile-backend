package com.friendship.service.impl;

import com.friendship.pojo.Code;
import com.friendship.pojo.Result;
import com.friendship.pojo.User;
import com.friendship.mapper.UserMapper;
import com.friendship.utils.MD5;
import com.friendship.utils.TokenRedis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@SuppressWarnings("all")
public class LoginSystemService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 根据token判断是否登录
     * @param token: 传过来的token
     * @return: 返回是否登录的信息
     */
    public Boolean loginOrOut(String token) {
        //去工具类判断token是否在redis中存在来判断是否登录
        return TokenRedis.hasLogin(stringRedisTemplate, token);
    }

    /**
     * 根据账号和密码来判断用户是否存在从而进行登录
     * @param account:账号
     * @param password:密码
     * @return: 返回是否登录成功的信息(如果登录成功还会带有生成的token信息)
     * @throws Exception
     */
    public Map<String, Object> userIsExists(String account, String password) throws Exception {
        Map<String, Object> map = new HashMap<>();
        //如果传过来的数据有一个为null则直接返回错误信息
        if (account == null || password == null) {
            map.put("loginStatus", false);
            return map;
        }
        String encryption = MD5.getEncryption(password);
        //去数据库判断用户是否存在
        int result = userMapper.userIsExists(account, encryption);
        //如果存在
        if (result == 1) {
            Map<String, Object> idByAccount = userMapper.getIdByAccount(account);
            String id = idByAccount.get("id") + "";
            //生成token,将token返回给前端,并且将token存入redis来保持用户的登录状态
            String token = UUID.randomUUID().toString().replaceAll("-", "") + id;
            TokenRedis.tokenToRedis(stringRedisTemplate, token, id, null);
            map.put("token", token);
            map.put("loginStatus", true);
        } else {
            map.put("loginStatus", false);
        }
        return map;
    }

    /**
     * 判断注册时的账号是否已经存在
     * @param account: 用户注册时输入的账号
     * @return: 返回此账号是否在数据库中已经存在
     */
    public Result accountIsExists(String account) {
        int result = userMapper.accountIsExists(account);
        if (result == 1) {
            return new Result(Code.ACCOUNT_EXIST.getCode(), "账号已经存在");
        }
        return new Result(Code.ACCOUNT_NON_EXIST.getCode(), "账号处于空闲状态");
    }

    /**
     * 用来进行用户的注册
     * @param user: 通过实体类封装的用户信息(账号, 密码等等)
     * @param errorResult: 包含输入数据是否合理的检验对象
     * @return: 返回注册的结果
     * @throws Exception
     */
    public Result registerUser(User user, BindingResult errorResult) throws Exception {
        //对数据进行校验
        if (!errorResult.hasErrors()) {
            //将account再次拿到数据库中进行校验是否已经存在
            int result1 = userMapper.accountIsExists(user.getAccount());
            //如果已经存在则直接返回错误信息
            if (result1 == 1) {
                return new Result(Code.INSERT_ERR.getCode(), "注册失败");
            }
            //对password进行加密
            user.setPassword(MD5.getEncryption(user.getPassword()));
//            将数据插入数据库中,从而完成用户的注册
            String format = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDate.now());
            user.setRegisterTime(format);
            int result = userMapper.insert(user);
            //生成token
            String id = userMapper.getIdByAccount(user.getAccount()).get("id") + "";
            String token = UUID.randomUUID().toString().replaceAll("-", "") + id;
            //将token存放到redis中,并且将用户的基本信息也存放到redis中
            Map<String, Object> user_info = userMapper.getAllInfoById(Long.valueOf(id));
            stringRedisTemplate.opsForList().leftPush("id_list", user_info.get("id") + "");
            user_info.put("id", user_info.get("id") + "");
            TokenRedis.tokenToRedis(stringRedisTemplate, token, id, user_info);
            Map<String, Object> map = new HashMap<>();
            //返回是否成功的结果和token
            map.put("token", token);
            return new Result(Code.INSERT_OK.getCode(), map);
        }
        return new Result(Code.INSERT_ERR.getCode(), "注册失败");
    }
}
