package com.friendship;

import com.friendship.mapper.*;
import com.friendship.pojo.UserGroup;
import com.friendship.service.impl.BlogService;
import com.friendship.service.impl.PersonalInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SpringApplicationTest {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ReplyMapper replyMapper;

    @Autowired
    private UserGroupMapper userGroupMapper;

    @Autowired
    private FriendlyRelationshipMapper friendlyRelationshipMapper;

    @Autowired
    private BlogService blogService;

    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    @Autowired
    private PersonalInfoService personalInfoService;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private BlogMapper blogMapper;


    @Test
    @MyAnnotation(age = 20)
    public void testDate(){
        LocalDateTime localDate = LocalDateTime.now();
        System.out.println(localDate);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String format = dateTimeFormatter.format(localDate);
        System.out.println(format);
        LocalDateTime parse = LocalDateTime.parse("2022-11-22 14:25:15", dateTimeFormatter);
        System.out.println(parse.isAfter(localDate));
    }


    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    @Transactional
    public void testTransaction() {
//        TransactionStatus transaction = platformTransactionManager.getTransaction(new DefaultTransactionDefinition());
//        try {
//            Integer integer = userGroupMapper.updateUserGroupMemberNumberById(6L);
//            int i = 1 / 0;
//            Integer s = userGroupMapper.updateUserGroupMemberNumberById(7L);
//            platformTransactionManager.commit(transaction);
//            System.out.println("成功了");
//        } catch (Exception e) {
//            platformTransactionManager.rollback(transaction);
//            System.out.println("出差了.......");
//        }

    }
}

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@interface MyAnnotation{
    String value() default "wujunjie";

    int age() default 19;
}
