package com.friendship.service.impl;

import com.friendship.mapper.BlogLikeMapper;
import com.friendship.mapper.BlogMapper;
import com.friendship.mapper.CommentLikeMapper;
import com.friendship.mapper.CommentMapper;
import com.friendship.pojo.BlogLike;
import com.friendship.pojo.CommentLike;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Service
public class LikeService {
    @Resource
    private BlogLikeMapper blogLikeMapper;

    @Resource
    private CommentLikeMapper commentLikeMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private BlogMapper blogMapper;

    @Resource
    private CommentMapper commentMapper;

    public int operateBlogLike(String token, Long blogId, int flag) {
        Long userId = Long.valueOf(Objects.requireNonNull(stringRedisTemplate.opsForValue().get(token)));
        //查询以前是否点赞过该博客
        //如果flag是0则代表删除该点赞信息
        if (flag == 0) {
            //将博客的点赞数量减1
            blogMapper.updateBlogLikeNumber(blogId, -1);
            return blogLikeMapper.updateLikeInfo(userId, blogId, 1);
        }
        //将博客的点赞数量加一
        blogMapper.updateBlogLikeNumber(blogId, 1);
        //为1则代表要新增点赞信息
        //首先查询从前用户是否点赞过该博客
        int i = blogLikeMapper.queryLikeBefore(userId, blogId);
        //如果点赞过, 则只要回复点赞记录即可
        if (i == 1) {
            return blogLikeMapper.updateLikeInfo(userId, blogId, 0);
        }
        //以前没有点赞过, 则新增点赞信息即可
        BlogLike blogLike = new BlogLike();
        blogLike.setLikeId(blogId);
        blogLike.setCreateTime(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()));
        blogLike.setUserId(userId);
        return blogLikeMapper.insert(blogLike);
    }

    public int operateCommentLike(String token, Long commentId, int flag) {
        Long userId = Long.valueOf(Objects.requireNonNull(stringRedisTemplate.opsForValue().get(token)));
        //查询以前是否点赞过该评论
        //如果flag是0则代表删除该点赞信息
        if (flag == 0) {
            //将博客的点赞数量减1
            commentMapper.updateCommentLikeNumber(commentId, -1);
            return commentLikeMapper.updateLikeInfo(userId, commentId, 1);
        }
        //将博客的点赞数量加一
        commentMapper.updateCommentLikeNumber(commentId, 1);
        //为1则代表要新增点赞信息
        //首先查询从前用户是否点赞过该评论
        int i = commentLikeMapper.queryLikeBefore(userId, commentId);
        //如果点赞过, 则只要回复点赞记录即可
        if (i == 1) {
            return commentLikeMapper.updateLikeInfo(userId, commentId, 0);
        }
        //以前没有点赞过, 则新增点赞信息即可
        CommentLike commentLike = new CommentLike();
        commentLike.setLikeId(commentId);
        commentLike.setCreateTime(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()));
        commentLike.setUserId(userId);
        return commentLikeMapper.insert(commentLike);
    }
}
