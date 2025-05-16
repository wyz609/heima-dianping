package com.hmdp.Interceptor;

import cn.hutool.core.bean.BeanUtil;
import com.hmdp.dto.UserDTO;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Class name: RefreshTokenInterceptor
 * Package: com.hmdp.Interceptor
 * Description:
 *
 * @Create: 2025/5/15 23:50
 * @Author: jay
 * @Version: 1.0
 */
public class RefreshTokenInterceptor implements HandlerInterceptor {

    // 这里不需要进行装配， 因为RefreshTokenInterceptor是我们自己手动在MvcConfig中进行new出来的
    private StringRedisTemplate stringRedisTemplate;

    public RefreshTokenInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取请求头中的token
        String token = request.getHeader("authorization");
        // 如果token是空的话，则未登录 直接放行 交给LoginInterceptor处理
        if(token == null){
            return true;
        }

        String key = RedisConstants.LOGIN_USER_KEY + token;
        // 基于token获取Redis中的用户数据
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(key);
        if(userMap.isEmpty()){
            // 如果用户不存在， 则直接放行
            return true;
        }
        // 查询到的Hash数据转化为UserDTO对象
        UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);
        // 将用户信息存入到ThreadLocal中
        UserHolder.saveUser(userDTO);
        // 刷新tokenTTL， 这里的存活时间根据需求自己设置， 这里的常量值是30分钟
        stringRedisTemplate.expire(key,RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES);

        // 刷新tokenTTL，这里的存活时间根据需要自己设置，这里的常量值是30分钟
        stringRedisTemplate.expire(key, 30, TimeUnit.MINUTES);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清除ThreadLocal中的用户信息
        UserHolder.removeUser();
    }
}
