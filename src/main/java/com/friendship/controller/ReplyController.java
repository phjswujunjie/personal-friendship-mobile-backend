package com.friendship.controller;

import com.friendship.accessControl.LoginRequired;
import com.friendship.pojo.Code;
import com.friendship.pojo.Reply;
import com.friendship.pojo.Result;
import com.friendship.service.impl.ReplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;


/**
 * 处理回复操作的类
 */
@RestController
@RequestMapping("/replies")
@CrossOrigin(originPatterns = {"*"}, allowCredentials = "true")
public class ReplyController {
    @Resource
    private ReplyService replyService;

    //创建回复
    @LoginRequired
    @PostMapping
    public Result createReply(@RequestBody Reply reply, HttpServletRequest request){
        if (Optional.ofNullable(request.getHeader("token")).isEmpty()){
            return new Result(Code.UNAUTHORIZED.getCode(), "没有登录");
        }
        Map<String, Object> replys = replyService.createReply(reply, request.getHeader("token"));
        if(Optional.ofNullable(replys.get("id")).isEmpty()){
            return new Result(Code.BAD_REQUEST.getCode(), "插入失败");
        }
        return new Result(Code.OK.getCode(), replys);
    }

    //根据评论的id得到该评论的全部回复信息
    @GetMapping("/{id}")
    public Result getReplybyCommentId(@PathVariable Long id){
        List<List<Map<String, Object>>> replyByCommentId = replyService.getReplyByCommentId(id);
        return new Result(Code.OK.getCode(), replyByCommentId);
    }
}
