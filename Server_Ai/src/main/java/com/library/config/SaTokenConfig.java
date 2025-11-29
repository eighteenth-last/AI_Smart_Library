package com.library.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 权限框架配置
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    /**
     * 注册 Sa-Token 拦截器，打开注解式鉴权功能
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册路由拦截器，自定义认证规则
        registry.addInterceptor(new SaInterceptor(handle -> {
            // 指定一套 match 规则
            SaRouter
                    // 登录、注册等接口放行
                    .match("/**")
                    .notMatch("/auth/login", 
                    "/auth/register", 
                    "/ai/chat", 
                    "/books/**", 
                    "/categories/**", 
                    "/authors/**",
                    "/uploads/**"  // 静态资源放行（图片、文件等）
                    )
                    // 检查是否登录
                    .check(r -> StpUtil.checkLogin());

            // 权限认证：不同接口访问权限
            SaRouter
                    .match("/admin/**")
                    // 排除不需要admin权限的接口
                    .notMatch(
                        "/admin/books/*",      // 单个图书详情（作者也可查看、编辑）
                        "/admin/author/**"     // 作者相关接口
                    )
                    // 权限码为 admin
                    .check(r -> StpUtil.checkRole("admin"));

            SaRouter
                    .match("/author/**")
                    // 权限码为 author
                    .check(r -> StpUtil.checkRole("author"));

        })).addPathPatterns("/**")
          .excludePathPatterns("/error", "/favicon.ico"); // 排除静态资源和错误页面
    }
}