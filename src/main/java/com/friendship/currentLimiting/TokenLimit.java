package com.friendship.currentLimiting;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 *  使用令牌桶进行限流, 参数列表中必须有HttpServletResponse类型的参数
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TokenLimit {
    String key();

    // 每秒的最大请求量
    int count() default 500;

    // 请求等待的最大时间
    int timeout() default 500;

    // 等待时间的时间单位
    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;
}
