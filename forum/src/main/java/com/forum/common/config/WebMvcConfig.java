package com.forum.common.config;

import com.forum.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")  // 拦截所有请求
                .excludePathPatterns(
                        "/user/login",
                        "/user/register",
                        "/test/ping",  // 添加测试接口到白名单
                        "/test/**",     // 测试模块全部放行
                        "/error",       // 错误页面放行
                        "/favicon.ico", // 网站图标放行
                        "/static/**",   // 静态资源
                        "/public/**"    // 公共资源
                );
    }
}