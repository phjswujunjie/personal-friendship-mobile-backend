package com.friendship.currentLimiting;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *  计数器限流::使用此注解方法中需要有HttpServletResponse类型的参数
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CounterLimit {
    // 1秒钟的最大请求次数
    int value() default 1000;

    // 限流接口所对应的key
    String key();
}
