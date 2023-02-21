package com.friendship.service.impl;

import com.friendship.mapper.BlogMapper;
import com.friendship.mapper.CommentMapper;
import com.friendship.mapper.ReplyMapper;
import com.friendship.pojo.Reply;
import com.google.gson.Gson;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReplyService {
    @Autowired
    private ReplyMapper replyMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private BlogMapper blogMapper;

    @Autowired
    HashOperations<String, Object, Object> opsForHash;

    @Autowired
    private Gson gson;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    public Map<String, Object> createReply(Reply reply, String token){
        Long userId = Long.valueOf(Objects.requireNonNull(stringRedisTemplate.opsForValue().get(token)));
        String format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());
        reply.setCreateTime(format);
        reply.setOwnerId(userId);
        replyMapper.insert(reply);
        // 更新博客的回复数
        blogMapper.updateBlogCommentNumber(reply.getBlogId());
        // 更新评论的回复数
        commentMapper.updateReplyNumber(reply.getCommentId());
        // 将Reply类型转换成Map, 再对Map进行处理返回给前端
        Map<String, Object> map = processReplyToMap(reply);
        return processReplyData(map);
    }

    public List<List<Map<String, Object>>> getReplyByCommentId(Long id){
        List<Map<String, Object>> replyByCommentId = replyMapper.getReplyByCommentId(id);
        // 处理回复信息
        List<Map<String, Object>> collect = replyByCommentId.stream().map(this::processReplyData).collect(Collectors.toCollection(ArrayList::new));
        // 开始处理子回复节点, 因为replyId为-1则为一级节点, 不为-1则为子回复节点
        // 新建一个容器用来存储回复信息, 一个List容器的第一个元素为一级回复节点, 后面的全为子回复节点
        Map<String, List<Map<String, Object>>> replyContainer = new HashMap<>();
        for (Map<String, Object> m : collect) {
            // 如果为一级回复节点, 则创建一个List容器放到replyContainer中, 并以回复的id值则为键
            if (("-1").equals(m.get("replyId") + "")) {
                List<Map<String, Object>> list = new ArrayList<>();
                list.add(m);
                replyContainer.put(m.get("id") + "", list);
            }
        }
        // 为什么在这里循环添加子回复节点? 而不是在上面的循环中的else块里添加? 因为可能子回复节点在collect中的位置所属一级节点之前, 添加子
        // 回复节点时则会报异常, 所以这里在一级回复节点添加完后再添加子回复节点
        for (Map<String, Object> m : collect) {
            // 如果为子回复节点, 则添加到所属一级回复节点所在的list容器中
            if (!("-1").equals(m.get("replyId") + "")) {
                replyContainer.get(m.get("replyId") + "").add(m);
            }
        }
        // 再将回复节点全部放到list容器中方便前端的处理
        List<List<Map<String, Object>>> replyList = new ArrayList<>();
        for (String key : replyContainer.keySet()) {
            replyList.add(replyContainer.get(key));
        }
        return replyList;
    }

    private Map<String, Object> processReplyData(Map<String, Object> p) {
        // 回复拥有者的信息
        p.put("ownerAvatar", "http://localhost:8888/static/upload/" + opsForHash.get("user_" + p.get("ownerId"), "avatar"));
        p.put("ownerNickname", opsForHash.get("user_" + p.get("ownerId"), "nickname"));
        p.put("ownerHomepage", "http://localhost:8082/u/" + p.get("ownerId"));
        // 回复对象的信息
        // 如果replyId不为-1才查询回复对象的信息, 为-1则代表为一级回复, 无需查询回复对象的信息
        if (("-1").equals(p.get("replyId") + "")) {
            return p;
        }
        p.put("replyObjectNickname", opsForHash.get("user_" + p.get("replyObjectId"), "nickname"));
        p.put("replyObjectHomepage", "http://localhost:8082/u/" + p.get("replyObjectId"));
        return p;
    }

    private Map<String, Object> processReplyToMap(Reply reply) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", reply.getId());
        map.put("content", reply.getContent());
        map.put("ownerId", reply.getOwnerId());
        map.put("commentId", reply.getCommentId());
        map.put("blogId", reply.getBlogId());
        map.put("blogOwnerId", reply.getBlogOwnerId());
        map.put("media", reply.getMedia());
        map.put("replyObjectId", reply.getReplyObjectId());
        map.put("replyId", reply.getReplyId());
        map.put("createTime", reply.getCreateTime());
        map.put("loveUser", reply.getLoveUser());
        map.put("loveUserNumber", reply.getLoveUserNumber());
        return map;
    }
}
