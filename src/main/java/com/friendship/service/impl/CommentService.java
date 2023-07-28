package com.friendship.service.impl;

import com.friendship.mapper.BlogMapper;
import com.friendship.mapper.CommentLikeMapper;
import com.friendship.mapper.CommentMapper;
import com.friendship.pojo.Comment;
import com.friendship.pojo.CommentLike;
import com.friendship.utils.CommonString;
import com.friendship.utils.TokenRedis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CommentService {

    @Resource
    private CommentMapper commentMapper;

    @Resource
    private CommentLikeMapper commentLikeMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private BlogMapper blogMapper;

    public Comment createComment(Comment comment, String token){
        Long ownerId = Long.valueOf(Objects.requireNonNull(stringRedisTemplate.opsForValue().get(token)));
        comment.setOwnerId(ownerId);
        blogMapper.updateBlogCommentNumber(comment.getBlogId());
        commentMapper.insert(comment);
        return comment;
    }

    public List<Map<String, Object>> getAllCommentByBlogId(Long id, HttpServletRequest request){
        List<Map<String, Object>> allCommentByBlogId = commentMapper.getAllCommentByBlogId(id);
        ArrayList<Map<String, Object>> collect = allCommentByBlogId.stream().map(p -> {
            p.put("avatar", CommonString.RESOURCES_ADDRESS + stringRedisTemplate.opsForHash().get("user_" + p.get("ownerId"), "avatar"));
            p.put("nickname", stringRedisTemplate.opsForHash().get("user_" + p.get("ownerId"), "nickname"));
            p.put("homepage", CommonString.FRONTEND_ADDRESS + "u/" + p.get("ownerId"));
            return p;
        }).collect(Collectors.toCollection(ArrayList::new));
        // 如果登录了则会查询点赞关系
        Boolean loginStatus = TokenRedis.hasLogin(stringRedisTemplate, request.getHeader("token"));
        if(loginStatus){
            collect.stream().map(p -> {
                p.put("isLike", commentLikeMapper.queryIsLike(Long.valueOf
                        (Objects.requireNonNull(stringRedisTemplate.opsForValue().get(request.getHeader("token")))),
                        Long.valueOf(p.get("commentId") + "")));
                p.put("loginStatus", true);
                return p;
            }).collect(Collectors.toCollection(ArrayList::new));
        } else {
            collect.stream().map(p -> {
                p.put("loginStatus", false);
                return p;
            }).collect(Collectors.toCollection(ArrayList::new));
        }
        return collect;
    }

    public List<Map<String, Object>> getCommentByPageSize(int pageNumber, int pageSize){
        int index = (pageNumber - 1) * pageSize;
        return commentMapper.queryCommentByPageSize(index, pageSize);
    }
}
