package com.friendship.controller;

import com.friendship.pojo.Code;
import com.friendship.pojo.Result;
import com.friendship.service.impl.ChatMessageService;
import com.friendship.websocket.ChatMessageContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 *  处理聊天信息的类
 */
@RestController
@RequestMapping("/chatMessages")
@CrossOrigin(originPatterns = {"*"}, allowCredentials = "true")
public class ChatMessageController {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping("/{toId}")
    public Result getChatMessages(@PathVariable Long toId, HttpServletRequest request){
        String token = request.getHeader("token");
        List<Map<String, Object>> chatMessage = chatMessageService.getChatMessage(toId, token);
        if (Optional.ofNullable(chatMessage).isPresent()){
            return new Result(Code.SELECT_OK.getCode(), chatMessage);
        }
        return new Result(Code.SELECT_ERR.getCode(), "非法请求");
    }

    @GetMapping("/queryMessageNumber")
    public Result queryMessageNumber(HttpServletRequest request){
        String id = stringRedisTemplate.opsForValue().get(request.getHeader("token"));
        if (Optional.ofNullable(id).isEmpty()){
            return new Result(Code.SELECT_ERR.getCode(), "没有token");
        }
        int messageNumber = 0;
        if (Optional.ofNullable(ChatMessageContainer.messageMap.get(Long.valueOf(id))).isPresent()){
            Map<Long, List<Map<String, Object>>> longListMap = ChatMessageContainer.messageMap.get(Long.valueOf(id));
            for ( List<Map<String, Object>> list : longListMap.values()) {
                messageNumber += list.size();
            }
        }
        return new Result(Code.SELECT_OK.getCode(), messageNumber);
    }

    @PostMapping("/storageUnreadMessage")
    public Result storageUnreadMessage(HttpServletRequest request, String content, Long fromId, String createTime, String avatar){
        chatMessageService.storageUnreadChatMessage(request, content, fromId, createTime, avatar);
        return new Result(Code.INSERT_OK.getCode(), "插入成功");
    }


}
