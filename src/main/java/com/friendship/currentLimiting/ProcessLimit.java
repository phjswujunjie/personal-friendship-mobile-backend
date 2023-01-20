package com.friendship.currentLimiting;

import com.friendship.pojo.Code;
import com.friendship.pojo.Result;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
@Aspect
@SuppressWarnings("all")
public class ProcessLimit {
    // 存储计数器的容器
    public static final ConcurrentHashMap<String, AtomicInteger> limitingMap = new ConcurrentHashMap();

    // 存储令牌桶的容器
    private static final ConcurrentHashMap<String, RateLimiter> tokenLimitingMap = new ConcurrentHashMap();

    /**
     * 计数器限流
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around("execution(* com.friendship.controller.*.*(..))")
    public Object processLimit(ProceedingJoinPoint joinPoint) throws Throwable {
        // 查看接口上是否有限流的注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        CounterLimit annotation = signature.getMethod().getAnnotation(CounterLimit.class);
        // 如果有限流的注解则开始对该接口进行限流
        if (Optional.ofNullable(annotation).isPresent()){
            String key = annotation.key();
            if (!limitingMap.containsKey(key)){
                limitingMap.putIfAbsent(key, new AtomicInteger(0));
            }
            AtomicInteger size = limitingMap.get(key);
            int count = annotation.value();
            if (size.get() >= count) {
                processResponseStatus(joinPoint.getArgs());
                return new Result(Code.MANY_REQUEST.getCode(), "请求太多了");
            } else {
                size.incrementAndGet();
            }
            if (size.get() >= count) {
                processResponseStatus(joinPoint.getArgs());
                return new Result(Code.MANY_REQUEST.getCode(), "请求太多了");
            }
        }
        return joinPoint.proceed();
    }

    /**
     *  使用令牌桶进行限流
     */
    @Around("execution(* com.friendship.controller.*.*(..))")
    public Object processTokenLimit(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        TokenLimit annotation = signature.getMethod().getAnnotation(TokenLimit.class);
        if (Optional.ofNullable(annotation).isPresent()){
            String key = annotation.key();
            if (!tokenLimitingMap.containsKey(key)){
                tokenLimitingMap.put(key, RateLimiter.create(annotation.count()));
            }
            if (!tokenLimitingMap.get(key).tryAcquire(annotation.timeout(), annotation.timeUnit())){
                processResponseStatus(joinPoint.getArgs());
                return new Result(Code.MANY_REQUEST.getCode(), "请求太多了");
            }
        }
        return joinPoint.proceed();
    }

    private void processResponseStatus(Object[] args){
        HttpServletResponse response = null;
        for (Object arg : args) {
            if (arg instanceof HttpServletResponse){
                response = (HttpServletResponse) arg;
            }
        }
        response.setStatus(429);
    }
}
