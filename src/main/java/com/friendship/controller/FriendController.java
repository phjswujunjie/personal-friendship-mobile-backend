package com.friendship.controller;

import com.friendship.currentLimiting.TokenLimit;
import com.friendship.pojo.Code;
import com.friendship.pojo.Result;
import com.friendship.service.impl.FriendService;
import com.friendship.websocket.ChatMessageContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * 处理好友关系的类
 */
@RestController
@SuppressWarnings("all")
@RequestMapping("/friends")
@CrossOrigin(originPatterns = {"http://localhost:8081/", "http://localhost:8082/"}, allowCredentials = "true")
public class FriendController {
    @Autowired
    private FriendService friendService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    //当用户访问其他用户主页时, 判断其他用户是否被使用者关注
    @GetMapping("/{otherId}")
    @TokenLimit(key = "/friends/otherId", timeout = 100)
    public Result queryIsFollower(@PathVariable Long otherId, HttpServletRequest request, HttpServletResponse response){
        String token = request.getHeader("token");
        int i = friendService.queryRelation(token, otherId);
        if(i == 2){
            return new Result(Code.IS_A_FRIEND.getCode(), "是朋友关系");
        }else if (i == 0){
            return new Result(Code.NOT_HAVE_RELATION.getCode(), "二者没有关系");
        } else if (i == 4) {
            return new Result(Code.IS_A_FOLLOW.getCode(), "访问者关注了被访问者");
        }else if (i == 8){
            return new Result(Code.IS_A_FANS.getCode(), "被访问者为访问者的粉丝");
        }else {
            return new Result(Code.LOGIN_ERR.getCode(), "没有登录");
        }
    }

    //添加关注
    @PostMapping("/{followId}")
    public Result addFriend(@PathVariable Long followId, HttpServletRequest request){
        String token = request.getHeader("token");
        int i = friendService.addFriend(token, followId);
        if (i == 1){
            return new Result(Code.INSERT_OK.getCode(), "添加成功");
        }else {
            return new Result(Code.INSERT_ERR.getCode(), "添加失败");
        }
    }

    //取消关注
    @DeleteMapping("/{followId}")
    public Result deleteFriend(@PathVariable Long followId, HttpServletRequest request){
        String token = request.getHeader("token");
        int i = friendService.deleteFriend(token, followId);
        if (i == 1){
            return new Result(Code.DELETE_OK.getCode(), "取消关注成功!");
        }else {
            return new Result(Code.DELETE_ERR.getCode(), "取消关注失败!");
        }
    }

    //得到用户的全部关注
    @GetMapping("/follow")
    public Result getAllFollowInfo(HttpServletRequest request){
        String token = request.getHeader("token");
        List<List<Map<String, Object>>> allFollowInfo = friendService.getAllFollowOrFansInfo(token, 1);
        return new Result(Code.SELECT_OK.getCode(), allFollowInfo);
    }

    @GetMapping
    public Result getAllFriendInfo(HttpServletRequest request){
        String token = request.getHeader("token");
        List<Map<String, Object>> allFollowInfo = friendService.getAllFriendInfo(token);
        return new Result(Code.SELECT_OK.getCode(), allFollowInfo);
    }

    @GetMapping("/fans")
    public Result getAllFansInfo(HttpServletRequest request){
        String token = request.getHeader("token");
        List<List<Map<String, Object>>> allFansInfo = friendService.getAllFollowOrFansInfo(token, 2);
        return new Result(Code.SELECT_OK.getCode(), allFansInfo);
    }

    @PostMapping ("/queryStatus")
    public Result getFriendsStatus(Long[] idList, HttpServletRequest request){
        Long userId = Long.valueOf(stringRedisTemplate.opsForValue().get(request.getHeader("token")));
        Map<Long, Boolean> map = new HashMap<>();
        Map<Long, Integer> messageNumberMap = new HashMap<>();
        Map<Long, Map<String, Object>> messageMap = new HashMap<>();
        for (Long id : idList) {
            map.put(id, ChatMessageContainer.userMap.containsKey(id));
        }
        Map<Long, List<Map<String, Object>>> messageListMap = ChatMessageContainer.messageMap.get(userId);
        for (Long id : idList) {
           if (messageListMap != null && messageListMap.get(id) != null){
               messageNumberMap.put(id, messageListMap.get(id).size());
               if(messageListMap.get(id).size() != 0) {
                   messageMap.put(id, messageListMap.get(id).get(messageListMap.get(id).size() - 1));
               }
           }else {
               messageNumberMap.put(id, 0);
           }
        }
        List<Map> mapList = new ArrayList<>();
        mapList.add(map);
        mapList.add(messageNumberMap);
        mapList.add(messageMap);
        return new Result(Code.SELECT_OK.getCode(), mapList);
    }
}
