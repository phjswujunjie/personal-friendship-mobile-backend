package com.friendship.controller;

import com.friendship.currentLimiting.CounterLimit;
import com.friendship.pojo.Code;
import com.friendship.pojo.Result;
import com.friendship.service.impl.BlogService;
import com.friendship.service.impl.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 处理对博客的增删查改操作的类
 */
@RestController
@SuppressWarnings("all")
@RequestMapping("/blogs")
@CrossOrigin(originPatterns = {"http://localhost:8081/", "http://localhost:63342/", "http://localhost:8082/"}, allowCredentials = "true")
public class BlogController {

    @Autowired
    private BlogService blogService;

    @Autowired
    private CommentService commentService;

    //供用户上传博客信息
    @PostMapping
    public Result createBlog(MultipartFile[] files, String text, HttpServletRequest request, String isPublic) throws Exception{
        String token = request.getHeader("token");
        int result = blogService.uploadBlog(files, text, isPublic, token);
        if (result == 1){
            return new Result(Code.INSERT_OK.getCode(), "创建成功");
        }else {
            return new Result(Code.INSERT_ERR.getCode(), "创建失败");
        }
    }

    //得到附近全部的博客信息
    @GetMapping("/around")
    public Result displayAroundBlog(HttpServletRequest request){
        List<Map<String, Object>> mapList = blogService.displayBlog(request);
        return new Result(Code.SELECT_OK.getCode(), mapList);
    }

    //根据id得到该用户的全部博客信息
    @GetMapping("/users/{id}")
    public Result displayBlogById(@PathVariable String id, HttpServletRequest request){
        List<Map<String, Object>> mapList = blogService.displayBlog(request, id);
        return new Result(Code.SELECT_OK.getCode(), mapList);
    }

    @GetMapping("/{id}")
    @CounterLimit(key = "/blogs/id", value = 100)
    public Result commentBlogById(@PathVariable Long id, HttpServletRequest request, HttpServletResponse response){
        //得到博客的全部评论
        List<Map<String, Object>> allComments = commentService.getAllCommentByBlogId(id, request);
        //得到博客的基本信息
        allComments.add(0, blogService.commentBlogById(request, id));
        return new Result(Code.SELECT_OK.getCode(), allComments);
    }

    @DeleteMapping("/{id}")
    public Result deleteBlogById(@PathVariable Long id){
        int result = blogService.deleteBlogById(id);
        if (result == 1){
            return new Result(Code.DELETE_OK.getCode(), "删除成功");
        }else {
            return new Result(Code.DELETE_ERR.getCode(), "删除失败");
        }
    }
}
