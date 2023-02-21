package com.friendship.controller;

import com.friendship.pojo.Code;
import com.friendship.pojo.Comment;
import com.friendship.pojo.Result;
import com.friendship.service.impl.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 *  处理评论的增删查改操作的类
 */
@RestController
@SuppressWarnings("all")
@RequestMapping("/comments")
@CrossOrigin(originPatterns = {"*"}, allowCredentials = "true")
public class CommentController {
    @Autowired
    private CommentService commentService;

    //添加评论
    @PostMapping
    public Result createComment(@RequestBody Comment comment, HttpServletRequest request){
        if (Optional.ofNullable(request.getHeader("token")).isEmpty()){
            return new Result(Code.LOGIN_ERR.getCode(), "没有登录");
        }
        Comment comment1 = commentService.createComment(comment, request.getHeader("token"));
        if (Optional.ofNullable(comment1.getId()).isPresent()){
            return new Result(Code.INSERT_OK.getCode(), comment1);
        }
        return new Result(Code.INSERT_ERR.getCode(), "插入失败");
    }

    //根据博客的id来获取它的全部评论
    @GetMapping("/{id}")
    public Result getAllCommentByBlogId(@PathVariable Long id, HttpServletRequest request){
        List<Map<String, Object>> allComments = commentService.getAllCommentByBlogId(id, request);
        return new Result(Code.SELECT_OK.getCode(), allComments);
    }

    @GetMapping("/page")
    public Result getCommentByPageSize(Integer pageNumber, Integer pageSize){
        List<Map<String, Object>> commentByPageSize = commentService.getCommentByPageSize(pageNumber, pageSize);
        return new Result(Code.SELECT_OK.getCode(), commentByPageSize);
    }
}
