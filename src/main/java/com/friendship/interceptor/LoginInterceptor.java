package com.friendship.interceptor;

import com.friendship.pojo.Code;
import com.friendship.pojo.Result;
import com.friendship.utils.TokenRedis;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }
        if ("/blogs".equals(request.getRequestURI()) && "GET".equals(request.getMethod())) {
            return true;
        }
        String token = request.getHeader("token");
        if (token != null) {
            Map<String, Object> map = TokenRedis.hasLogin(stringRedisTemplate, token);
            if ((boolean) map.get("loginStatus")) {
                return true;
            }
        }
        Gson gson = new Gson();
        response.setContentType("application/json;charset=utf-8");
        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.write(gson.toJson(new Result(Code.LOGIN_ERR.getCode(), "登录信息失效, 请重新登陆")).getBytes(StandardCharsets.UTF_8));
        return false;
    }
}
