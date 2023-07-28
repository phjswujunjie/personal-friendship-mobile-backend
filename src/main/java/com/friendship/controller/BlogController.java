package com.friendship.controller;

import com.friendship.accessControl.LoginRequired;
import com.friendship.currentLimiting.CounterLimit;
import com.friendship.currentLimiting.TokenLimit;
import com.friendship.pojo.Code;
import com.friendship.pojo.Result;
import com.friendship.service.impl.BlogService;
import com.friendship.service.impl.CommentService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 处理对博客的增删查改操作的类
 */
@RestController
@RequestMapping("/blogs")
@CrossOrigin(originPatterns = {"*"}, allowCredentials = "true")
public class BlogController {

    @Resource
    private BlogService blogService;

    @Resource
    private CommentService commentService;

    //供用户上传博客信息
    @LoginRequired
    @PostMapping
    public Result createBlog(MultipartFile[] files, String text, HttpServletRequest request, String isPublic) throws Exception{
        String token = request.getHeader("token");
        int result = blogService.uploadBlog(files, text, isPublic, token);
        if (result == 1){
            return new Result(Code.OK.getCode(), "创建成功");
        }else {
            return new Result(Code.BAD_REQUEST.getCode(), "创建失败");
        }
    }

    //得到附近全部的博客信息
    @GetMapping("/around")
    @TokenLimit(key = "/around")
    public Result displayAroundBlog(HttpServletRequest request, HttpServletResponse response){
        List<Map<String, Object>> mapList = blogService.displayBlog(request);
        return new Result(Code.OK.getCode(), mapList);
    }

    @LoginRequired
    @GetMapping("/follow")
    public Result displayFollowBlog(HttpServletRequest request){
        List<Map<String, Object>> mapList = blogService.getBlogsOfFollowers(request.getHeader("token"));
        return new Result(Code.OK.getCode(), mapList);
    }

    @LoginRequired
    //根据id得到该用户的全部博客信息
    @GetMapping("/users/{id}")
    public Result displayBlogById(@PathVariable String id, HttpServletRequest request){
        List<Map<String, Object>> mapList = blogService.displayBlog(request, id);
        return new Result(Code.OK.getCode(), mapList);
    }

    @GetMapping("/{id}")
    @CounterLimit(key = "/blogs/id", value = 100)
    public Result commentBlogById(@PathVariable Long id, HttpServletRequest request, HttpServletResponse response){
        //得到博客的全部评论
        List<Map<String, Object>> allComments = commentService.getAllCommentByBlogId(id, request);
        //得到博客的基本信息
        allComments.add(0, blogService.commentBlogById(request, id));
        return new Result(Code.OK.getCode(), allComments);
    }

    @LoginRequired
    @DeleteMapping("/{id}")
    public Result deleteBlogById(@PathVariable Long id, HttpServletRequest request){
        int result = blogService.deleteBlogById(id);
        if (result == 1){
            return new Result(Code.OK.getCode(), "删除成功");
        }else {
            return new Result(Code.BAD_REQUEST.getCode(), "删除失败");
        }
    }
}
