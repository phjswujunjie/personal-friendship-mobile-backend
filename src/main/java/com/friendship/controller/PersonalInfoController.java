package com.friendship.controller;

import com.friendship.accessControl.LoginRequired;
import com.friendship.currentLimiting.CounterLimit;
import com.friendship.pojo.Code;
import com.friendship.pojo.Result;
import com.friendship.pojo.User;
import com.friendship.service.impl.PersonalInfoService;
import com.friendship.utils.TokenRedis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 处理用户个人信息的增删查改操作的类
 */
@RestController
@RequestMapping("/users")
@CrossOrigin(originPatterns = {"*"}, allowCredentials = "true")
public class PersonalInfoController {

    @Resource
    private PersonalInfoService personalInfoService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 根据token得到是否登录的状态信息,如果登录,则返回头像的地址,没有登录则展示没有登录的图标信息
     * @param request
     * @return
     */
    @LoginRequired
    @GetMapping("/avatars")
    public Result getAvatar(HttpServletRequest request){
        String token = request.getHeader("token");
        //将token带到Service层去redis中校验是否登录,登录则获取头像的地址
        Map<String, Object> avatar = personalInfoService.getAvatar(token);
        //将得到的结果返回给前端
        return new Result(Code.OK.getCode(), avatar);
    }

    /**
     * 实现用户头像的上传
     * @param avatar:前端传过来的裁剪图片的base64格式的数据
     * @param request
     * @return
     * @throws Exception
     */
    @LoginRequired
    @PutMapping("/avatars")
    public Result updateAvatar(String avatar, HttpServletRequest request) throws Exception{
        //得到token
        String token = request.getHeader("token");
        Object result = personalInfoService.uploadAvatar(avatar, token);
        return new Result(Code.OK.getCode(), result);
    }

    //得到用户的全部信息
    @LoginRequired
    @GetMapping
    @CounterLimit(key = "users", value = 100)
    public Result getUserInfo(HttpServletRequest request, HttpServletResponse response){
        String token = request.getHeader("token");
        Map<Object, Object> allInfo = personalInfoService.getUserInfo(token);
        return new Result(Code.OK.getCode(), allInfo);
    }

    /**
     * 访问用户主页根据id来得到用户的全部信息
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/{id}")
    public Result getUserInfoById(@PathVariable String id, HttpServletRequest request){
        Map<Object, Object> allInfo = personalInfoService.getUserInfoById(id);
        if (allInfo.size() == 2){
            return new Result(Code.BAD_REQUEST.getCode(), "没有该用户");
        }
        if(request.getHeader("token") != null) {
            boolean token = TokenRedis.isSelf(stringRedisTemplate.opsForValue(), request.getHeader("token"), Long.valueOf(id));
            allInfo.put("isSelf", token);
        }else {
            allInfo.put("isSelf", false);
        }
        return new Result(Code.OK.getCode(), allInfo);
    }

    //更新用户的信息
    @LoginRequired
    @PutMapping
    public Result updateUserInfo(HttpServletRequest request, User user){
        String token = request.getHeader("token");
        int result = personalInfoService.updateUserInfo(token, user);
        if (result == 1){
            return new Result(Code.OK.getCode(), "更新成功");
        }else {
            return new Result(Code.BAD_REQUEST.getCode(), "更新失败");
        }
    }

    @LoginRequired
    @PutMapping("/change/{flag}")
    public Result updateUserGender(@PathVariable Integer flag,HttpServletRequest request, String newVal){
        String token = request.getHeader("token");
        System.out.println("flag is" + flag);
        Long userId = Long.valueOf(stringRedisTemplate.opsForValue().get(token));
        Integer result = personalInfoService.updateUserInfoByFlag(userId, flag, newVal);
        if (result == 1){
            return new Result(Code.OK.getCode(), "修改成功");
        }else {
            return new Result(Code.BAD_REQUEST.getCode(), "修改失败");
        }
    }

    //搜索查询用户
    @GetMapping("/search/{condition}")
    public Result searchUser(@PathVariable String condition){
        return new Result(Code.OK.getCode(),  personalInfoService.searchUserByCondition(condition));
    }
}
