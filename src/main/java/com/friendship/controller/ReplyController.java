package com.friendship.controller;

import com.friendship.pojo.Code;
import com.friendship.pojo.Reply;
import com.friendship.pojo.Result;
import com.friendship.service.impl.ReplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;


/**
 * 处理回复操作的类
 */
@RestController
@SuppressWarnings("all")
@RequestMapping("/replies")
@CrossOrigin(originPatterns = {"http://localhost:8081/", "http://localhost:8082/"}, allowCredentials = "true")
public class ReplyController {
    @Autowired
    private ReplyService replyService;

    //创建回复
    @PostMapping
    public Result createReply(@RequestBody Reply reply, HttpServletRequest request){
        if (Optional.ofNullable(request.getHeader("token")).isEmpty()){
            return new Result(Code.LOGIN_ERR.getCode(), "没有登录");
        }
        Long replyId = replyService.createComment(reply, request.getHeader("token"));
        if(Optional.ofNullable(replyId).isEmpty()){
            return new Result(Code.INSERT_ERR.getCode(), "插入失败");
        }
        return new Result(Code.INSERT_OK.getCode(), replyId);
    }

    //根据评论的id得到该评论的全部回复信息
    @GetMapping("/{id}")
    public Result getReplybyCommentId(@PathVariable Long id){
        List<Map<String, Object>> replyByCommentId = replyService.getReplyByCommentId(id);
        return new Result(Code.SELECT_OK.getCode(), replyByCommentId);
    }
}
