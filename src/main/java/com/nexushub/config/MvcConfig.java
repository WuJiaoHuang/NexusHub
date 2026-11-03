package com.nexushub.config;

import com.nexushub.interceptor.LoginInterceptor;
import com.nexushub.interceptor.RefreshTokenInterceptor;
import com.nexushub.interceptor.AdminAuthorizationInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class MvcConfig implements WebMvcConfigurer {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private AdminAuthorizationInterceptor adminAuthorizationInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Only explicitly annotated handler methods allow anonymous access.
        registry.addInterceptor(new LoginInterceptor()).addPathPatterns("/**").order(1);
        registry.addInterceptor(adminAuthorizationInterceptor).addPathPatterns("/**").order(2);
        //token刷新的拦截器
        registry.addInterceptor(new RefreshTokenInterceptor(stringRedisTemplate))
                .addPathPatterns("/**").order(0);
    }
}
