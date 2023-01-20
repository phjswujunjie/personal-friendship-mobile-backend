package com.friendship.controller;

import com.friendship.pojo.Code;
import com.friendship.pojo.Result;
import com.friendship.pojo.User;
import com.friendship.mapper.UserMapper;
import com.friendship.service.impl.LoginSystemService;
import com.friendship.utils.TokenRedis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 处理注册登录以及权限控制的类
 */
@RestController
@CrossOrigin(originPatterns = {"http://localhost:8081", "http://localhost:8082/"}, allowCredentials = "true")
@SuppressWarnings("all")
public class LoginSystemController {

    @Autowired
    private LoginSystemService loginSystemService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private UserMapper userMapper;

    /**
     * 检查是否登录
     *
     * @param request
     * @return
     */
    @RequestMapping("/loginOrOut")
    public Result loginOrOut(HttpServletRequest request) {
        //得到前端带过来的token
        String token = request.getHeader("token");
        //去业务层进行判断
        Map<String, Object> map = loginSystemService.loginOrOut(token);
        if ((boolean) map.get("loginStatus")) {
            return new Result(Code.LOGIN_OK.getCode(), "登陆成功");
        }
        return new Result(Code.LOGIN_ERR.getCode(), "登陆信息不正确");
    }

    /**
     * 检查账号和密码是否存在
     *
     * @param account
     * @param password
     * @return
     * @throws Exception
     */
    @RequestMapping("/loginExamine")
    public Result loginExamine(String account, String password) throws Exception {
        Map<String, Object> map = loginSystemService.userIsExists(account, password);
        if ((boolean) map.get("loginStatus")) {
            map.remove("loginStatus");
            return new Result(Code.LOGIN_OK.getCode(), map);
        }
        return new Result(Code.LOGIN_ERR.getCode(), "登陆信息不正确");
    }

    /**
     * 判断注册时用户输入的账号是否已经存在
     *
     * @param account:前端传过来的账号
     * @return
     */
    @RequestMapping("/accountIsExists")
    public Result accountIsExists(String account) {
        return loginSystemService.accountIsExists(account);
    }

    /**
     * 实现用户的注册
     *
     * @param user:传过来的注册信息
     * @param result
     * @return
     * @throws Exception
     */
    @RequestMapping("/registerUser")
    public Result registerUser(@Valid User user, BindingResult result) throws Exception {
        return loginSystemService.registerUser(user, result);
    }

    /**
     * 判断访问主页的用户是否是该主页的主人
     * @param id: 主页拥有者的id
     * @param request: 包含token的请求对象
     * @return
     */
    //判断是否为本人
    @RequestMapping("/isSelf/{id}")
    public Result isSelf(@PathVariable Long id, HttpServletRequest request){
        Map<String, Object> map = new HashMap<>();
        boolean token = TokenRedis.isSelf(stringRedisTemplate.opsForValue(), request.getHeader("token"),id);
       if (token){
           return new Result(Code.IS_SELF.getCode(), "是本人");
       }
       return new Result(Code.NOT_SELF.getCode(), "不是本人");
    }
}
