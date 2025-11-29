package com.library.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;

/**
 * Web配置类 - CORS跨域配置
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * CORS Filter - 处理 OPTIONS 预检请求
     */
    @Bean
    @Order(-100) // 确保在 Sa-Token 拦截器之前执行
    public Filter corsFilter() {
        return new Filter() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                    throws IOException, ServletException {
                HttpServletRequest httpRequest = (HttpServletRequest) request;
                HttpServletResponse httpResponse = (HttpServletResponse) response;

                // 设置 CORS 头
                httpResponse.setHeader("Access-Control-Allow-Origin", httpRequest.getHeader("Origin"));
                httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
                httpResponse.setHeader("Access-Control-Allow-Headers", "*");
                httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
                httpResponse.setHeader("Access-Control-Max-Age", "3600");

                // 如果是 OPTIONS 请求，直接返回 200
                if ("OPTIONS".equals(httpRequest.getMethod())) {
                    httpResponse.setStatus(HttpServletResponse.SC_OK);
                    return;
                }

                chain.doFilter(request, response);
            }
        };
    }
}
