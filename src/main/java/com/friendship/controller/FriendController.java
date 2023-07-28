package com.friendship.controller;

import com.friendship.accessControl.LoginRequired;
import com.friendship.currentLimiting.TokenLimit;
import com.friendship.pojo.Code;
import com.friendship.pojo.Result;
import com.friendship.service.impl.FriendService;
import com.friendship.websocket.ChatMessageContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * 处理好友关系的类
 */
@RestController
@RequestMapping("/friends")
@CrossOrigin(originPatterns = {"*"}, allowCredentials = "true")
public class FriendController {
    @Resource
    private FriendService friendService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    //当用户访问其他用户主页时, 判断其他用户是否被使用者关注
    @GetMapping("/{otherId}")
    @TokenLimit(key = "/friends/otherId", timeout = 100)
    public Result queryIsFollower(@PathVariable Long otherId, HttpServletRequest request, HttpServletResponse response){
        String token = request.getHeader("token");
        int i = friendService.queryRelation(token, otherId);
        if(i == 50003){
            return new Result(Code.IS_A_FRIEND.getCode(), "是朋友关系");
        }else if (i == 50000){
            return new Result(Code.NOT_HAVE_RELATION.getCode(), "二者没有关系");
        } else if (i == 50002) {
            return new Result(Code.IS_A_FOLLOW.getCode(), "访问者关注了被访问者");
        }else if (i == 50001){
            return new Result(Code.IS_A_FANS.getCode(), "被访问者为访问者的粉丝");
        }else {
            return new Result(Code.UNAUTHORIZED.getCode(), "没有登录");
        }
    }

    //添加关注
    @LoginRequired
    @PostMapping("/{followId}")
    public Result addFriend(@PathVariable Long followId, HttpServletRequest request){
        String token = request.getHeader("token");
        int i = friendService.addFriend(token, followId);
        if (i == 1){
            return new Result(Code.OK.getCode(), "添加成功");
        }else {
            return new Result(Code.BAD_REQUEST.getCode(), "添加失败");
        }
    }

    //取消关注
    @LoginRequired
    @DeleteMapping("/{followId}")
    public Result deleteFriend(@PathVariable Long followId, HttpServletRequest request){
        String token = request.getHeader("token");
        int i = friendService.deleteFriend(token, followId);
        if (i == 1){
            return new Result(Code.OK.getCode(), "取消关注成功!");
        }else {
            return new Result(Code.BAD_REQUEST.getCode(), "取消关注失败!");
        }
    }

    //得到用户的全部关注
    @LoginRequired
    @GetMapping("/follow")
    public Result getAllFollowInfo(HttpServletRequest request){
        String token = request.getHeader("token");
        List<List<Map<String, Object>>> allFollowInfo = friendService.getAllFollowOrFansInfo(token, 1);
        return new Result(Code.OK.getCode(), allFollowInfo);
    }


    @LoginRequired
    @GetMapping
    public Result getAllFriendInfo(HttpServletRequest request){
        String token = request.getHeader("token");
        List<List<Map<String, Object>>> allFollowInfo = friendService.getAllFriendInfo(token);
        return new Result(Code.OK.getCode(), allFollowInfo);
    }

    @LoginRequired
    @GetMapping("/fans")
    public Result getAllFansInfo(HttpServletRequest request){
        String token = request.getHeader("token");
        List<List<Map<String, Object>>> allFansInfo = friendService.getAllFollowOrFansInfo(token, 2);
        return new Result(Code.OK.getCode(), allFansInfo);
    }

    @LoginRequired
    @PostMapping ("/queryStatus")
    public Result getFriendsAndGroupStatus(Long[] idList, HttpServletRequest request){
        Long userId = Long.valueOf(stringRedisTemplate.opsForValue().get(request.getHeader("token")));
        // 查询用户和群组的未读信息和未读数量
        List<Map> mapList = friendService.queryStatus(idList, userId);
        return new Result(Code.OK.getCode(), mapList);
    }
}
