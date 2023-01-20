package com.friendship;

import com.friendship.mapper.*;
import com.friendship.pojo.Blog;
import com.friendship.pojo.FriendlyRelationship;
import com.friendship.service.impl.FriendService;
import com.friendship.service.impl.PersonalInfoService;
import com.google.gson.Gson;
import org.apache.ibatis.annotations.Mapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.stream.Collectors;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SpringApplicationTest {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FriendlyRelationshipMapper friendlyRelationshipMapper;

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

//    @Test
//    public void test() {
//        HashOperations<String, Object, Object> hashOperations = stringRedisTemplate.opsForHash();
//        long l = System.currentTimeMillis();
//        for (int i = 0; i < 200000; i++) {
//            Map<String, Object> allInfoById = userMapper.getAllInfoById(3L);
//        }
//        long l1 = System.currentTimeMillis();
//        for (int i = 0; i < 200000; i++) {
//            Map<Object, Object> user_3 = hashOperations.entries("user_3");
//        }
//        long l2 = System.currentTimeMillis();
//        System.out.println("数据库:l1 - l:  " + (l1 - l));
//        System.out.println("redis:l2 - l1:  " + (l2 - l1));
//    }

//    @Test
//    public void redis() {
//        Map<String, Object> allInfoById = userMapper.getAllInfoById(3L);
//        Map<String, Object> allInfoById1 = userMapper.getAllInfoById(4L);
//        Map<String, Object> allInfoById2 = userMapper.getAllInfoById(11L);
//        Map<String, Object> allInfoById3 = userMapper.getAllInfoById(18L);
//        HashOperations<String, Object, Object> hashOperations = stringRedisTemplate.opsForHash();
//        allInfoById.put("id", allInfoById.get("id") + "");
//        allInfoById1.put("id", allInfoById1.get("id") + "");
//        allInfoById2.put("id", allInfoById2.get("id") + "");
//        allInfoById3.put("id", allInfoById3.get("id") + "");
//        hashOperations.putAll("user_3", allInfoById);
//        hashOperations.putAll("user_4", allInfoById1);
//        hashOperations.putAll("user_11", allInfoById2);
//        hashOperations.putAll("user_18", allInfoById3);
//        user_3.put("id", "3");
//        hashOperations.putAll("user_3", user_3);
//        Map<Object, Object> user_2 = hashOperations.entries("user_3");
//        System.out.println(user_2);
//        Map<Object, Object> userInfoById = personalInfoService.getUserInfoById("4");
//        System.out.println(userInfoById);
//    }

//    @Test
//    public void testStream() {
//        String name = "wujunjie";
//        Optional<String> optional = Optional.ofNullable(name);
//        optional.ifPresentOrElse(System.out::println, () -> {
//            System.out.println("false");
//        });
//        optional.filter(p -> {
//            return p.equals("wujunjie");
//        }).ifPresent(System.out::println);
//    }

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

    @Test
    public void testAnnotation(){
        Class<SpringApplicationTest> springApplicationTestClass = SpringApplicationTest.class;
        Annotation[] annotations = springApplicationTestClass.getAnnotations();
        for (Annotation annotation : annotations) {
            System.out.println(annotation);
        }
        Method[] methods = springApplicationTestClass.getMethods();
        for (Method method : methods) {
            if (method.getName().equals("testDate")){
                MyAnnotation annotation = method.getAnnotation(MyAnnotation.class);
                System.out.println(annotation.value());
                System.out.println(annotation.age());
            }
        }
    }

    @Test
    public void testId(){
//        FriendlyRelationship friendlyRelationship = new FriendlyRelationship(4L, 11L, "2022-11-24");
//        friendlyRelationshipMapper.insert(friendlyRelationship);
//        System.out.println(friendlyRelationship.getId());
        int a = 2;
        a <<= 2;
        System.out.println(a << 1);
    }

    @Test
    public void testById(){
      String a;
      a = "a";
    }

}

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@interface MyAnnotation{
    String value() default "wujunjie";

    int age() default 19;
}