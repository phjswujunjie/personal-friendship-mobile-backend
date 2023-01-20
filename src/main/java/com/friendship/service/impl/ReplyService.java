package com.friendship.service.impl;

import com.friendship.mapper.CommentMapper;
import com.friendship.mapper.ReplyMapper;
import com.friendship.pojo.Reply;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ReplyService {
    @Autowired
    private ReplyMapper replyMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    public Long createComment(Reply reply, String token){
        Long userId = Long.valueOf(Objects.requireNonNull(stringRedisTemplate.opsForValue().get(token)));
        String format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());
        reply.setCreateTime(format);
        reply.setOwnerId(userId);
        replyMapper.insert(reply);
        commentMapper.updateReplyNumber(reply.getCommentId());
        return reply.getId();
    }

    public List<Map<String, Object>> getReplyByCommentId(Long id){
        return replyMapper.getReplyByCommentId(id).stream().map(p -> {
            p.put("avatar", "http://localhost:8888/static/upload/" + stringRedisTemplate.opsForHash().get("user_" + p.get("ownerId"), "avatar"));
            p.put("nickname", stringRedisTemplate.opsForHash().get("user_" + p.get("ownerId"), "nickname"));
            p.put("homepage", "http://localhost:8081/u/" + p.get("ownerId") + "/blog");
            return p;
        }).collect(Collectors.toCollection(ArrayList::new));
    }
}
