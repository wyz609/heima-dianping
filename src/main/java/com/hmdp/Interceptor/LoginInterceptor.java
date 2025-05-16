package com.hmdp.Interceptor;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.hmdp.constant.UserConstant;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(UserHolder.getUser() == null) {
            response.setStatus(401);
            return false;
        }
        // 存在则直接放行
        return true;

//         获取请求头中的token
//        String token = request.getHeader("authorization");
//        // 如果token是空的话，则未登录
//        if(StrUtil.isBlank(token)){
//            response.setStatus(401);
//            return false;
//        }
//
//        String key = RedisConstants.LOGIN_USER_KEY + token;
//
//        // 基于token获取Redis中的用户数据
//        Map<Object, Object> userMap = redisTemplate.opsForHash().entries(key);
//        // 判断用户是否存在， 不存在， 则拦截
//        if(userMap.isEmpty()){
//            response.setStatus(401);
//            return false;
//        }
////     将查询到的数据转化为UserDTO对象
//        UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);
//        // 将用户信息存储到ThreadLocal中
//        UserHolder.saveUser(userDTO);
//        // 刷新tokenTTL，这里的存活时间根据需要自己设置，这里的常量值是30分钟
//        redisTemplate.expire(key,RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES);
//        return true;

//        // 1.获取session
//        HttpSession session = request.getSession();
//        // 2.获取session中存放的用户信息
////      User user =(User)session.getAttribute(UserConstant.USER_LOGIN);
//        UserDTO user = (UserDTO) session.getAttribute(UserConstant.USER_LOGIN);
//        // 3.判断用户是否存在
//        if(user == null){
//            // 不存在则进行拦截
//            response.setStatus(401);
//            return false;
//        }
//        // 4.用户存在，则保存用户信息到ThreadLocal中， UserHolder是提供好的工具类
//        UserHolder.saveUser(user);
//        // 5.放行
//        return true;
    }

    // 6.在请求处理完成后，清除ThreadLocal中的用户信息避免内存泄漏
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 6.清除ThreadLocal中的用户信息
        UserHolder.removeUser();
    }

}
