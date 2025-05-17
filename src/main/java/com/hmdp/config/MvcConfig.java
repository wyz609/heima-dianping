/**
 * Class name: MvcConfig
 * Package: com.hmdp.config
 * Description:
 *
 * @Create: 2025/5/15 15:44
 * @Author: jay
 * @Version: 1.0
 */
package com.hmdp.config;

import com.hmdp.Interceptor.LoginInterceptor;
import com.hmdp.Interceptor.RefreshTokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    // 在这里进行自动装配
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 添加拦截器
        registry.addInterceptor(new LoginInterceptor())
                .excludePathPatterns("/user/code",
                                    "/user/login",
                                    "/blog/hot",
                                    "/shop/**",
                                    "/shop-type/**",
                                    "/upload/**",
                                    "/voucher/**").order(1);

        registry.addInterceptor(new RefreshTokenInterceptor(stringRedisTemplate)).addPathPatterns("/**").order(0);
    }

}