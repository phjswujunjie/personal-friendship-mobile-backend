package com.friendship.controller;

import com.friendship.pojo.Code;
import com.friendship.pojo.Result;
import com.friendship.service.impl.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 *  处理点赞博客, 评论, 回复操作的类
 */
@RestController
@SuppressWarnings("all")
@RequestMapping("/likes")
@CrossOrigin(originPatterns = {"*"}, allowCredentials = "true")
public class LikeController {

    @Autowired
    private LikeService likeService;

    /**
     * 控制博客点赞的创建与删除
     * @param request
     * @param blogId: 博客id
     * @param flag: 1为新建点赞信息, 0为删除信息
     * @return
     */
    @PostMapping("/blog")
    public Result operateBlogLike(HttpServletRequest request, Long blogId, int flag){
        System.out.println(blogId);
        int i = likeService.operateBlogLike(request.getHeader("token"), blogId, flag);
        if (i == 0){
            return new Result(Code.INSERT_OK.getCode(), "插入成功");
        }else {
            return new Result(Code.INSERT_ERR.getCode(), "插入失败");
        }
    }

    @PostMapping("/comment")
    public Result operateCommentLike(HttpServletRequest request, Long commentId, int flag){
        int i = likeService.operateCommentLike(request.getHeader("token"), commentId, flag);
        if (i == 0){
            return new Result(Code.INSERT_OK.getCode(), "插入成功");
        }else {
            return new Result(Code.INSERT_ERR.getCode(), "插入失败");
        }
    }
}
