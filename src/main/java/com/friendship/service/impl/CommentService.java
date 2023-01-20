package com.friendship.service.impl;

import com.friendship.mapper.CommentLikeMapper;
import com.friendship.mapper.CommentMapper;
import com.friendship.pojo.Comment;
import com.friendship.pojo.CommentLike;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private CommentLikeMapper commentLikeMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public Long createComment(Comment comment, String token){
        Long ownerId = Long.valueOf(Objects.requireNonNull(stringRedisTemplate.opsForValue().get(token)));
        comment.setOwnerId(ownerId);
        commentMapper.insert(comment);
        return comment.getId();
    }

    public List<Map<String, Object>> getAllCommentByBlogId(Long id, HttpServletRequest request){
        List<Map<String, Object>> allCommentByBlogId = commentMapper.getAllCommentByBlogId(id);
        ArrayList<Map<String, Object>> collect = allCommentByBlogId.stream().map(p -> {
            p.put("avatar", "http://localhost:8888/static/upload/" + stringRedisTemplate.opsForHash().get("user_" + p.get("ownerId"), "avatar"));
            p.put("nickname", stringRedisTemplate.opsForHash().get("user_" + p.get("ownerId"), "nickname"));
            p.put("homepage", "http://localhost:8081/u/" + p.get("ownerId") + "/blog");
            return p;
        }).collect(Collectors.toCollection(ArrayList::new));
        if(request.getHeader("token") != null && stringRedisTemplate.opsForValue().get(request.getHeader("token")) != null){
            collect.stream().map(p -> {
                p.put("isLike", commentLikeMapper.queryIsLike(Long.valueOf
                        (Objects.requireNonNull(stringRedisTemplate.opsForValue().get(request.getHeader("token")))),
                        Long.valueOf(p.get("commentId") + "")));
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
