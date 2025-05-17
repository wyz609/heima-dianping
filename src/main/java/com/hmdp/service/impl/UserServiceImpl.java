package com.hmdp.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.MailUtils;
import com.hmdp.utils.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.hmdp.constant.RedisConstants.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {


    @Resource
    private StringRedisTemplate stringRedisTemplate;

//    private final IUserService iUserService;
//
//    public UserServiceImpl(IUserService iUserService) {
//        this.iUserService = iUserService;
//    }

    /**
     * 发送手机验证码
     *
     * @param phone   手机号
     * @param session 会话
     * @return 结果
     */
    @Override
    public Result sendCode(String phone, HttpSession session) throws Exception {
        // 在这里前期使用邮箱代替手机号码
        if (RegexUtils.isEmailInvalid(phone)) {
            return Result.fail("电话格式错误");
        }

        //1.校验手机号码
        // 校验方法一
//        if(phone == null || !phone.matches("1[3-9]\\d{9}")){
//            return Result.fail("手机号格式错误");
//        }
        // 校验方法二
//        if(RegexUtils.isPhoneInvalid(phone)){
//            return Result.fail("手机号格式错误");
//        }
        //2.方法一 生成验证码
//        String code = RandomUtil.randomNumbers(6);
        //2. 方法二 使用工具类生成随机验证码
        String code = MailUtils.achieveCode();
        //3.将验证码保存到Session中
//        session.setAttribute(UserConstant.USER_LOGIN_CODE_KEY, code);
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY + phone, code,LOGIN_CODE_TTL, TimeUnit.MINUTES);
        log.info("发送验证码成功，验证码：{}", code);
        // TODO 调用阿里云，将短信信息发送到指定手机
        // 这里使用邮件发送的方式进行接收验证码
        MailUtils.sendTestMail(phone, code);
        return Result.ok();
    }

    /**
     * 登录功能
     *
     * @param loginForm 登录参数，包含手机号、验证码；或者手机号、密码
     */
    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        //获取登录账号
        String phone = loginForm.getPhone();
        //获取登录验证码
        String code = loginForm.getCode();
//        //获取session中的验证码
//        Object cacheCode = session.getAttribute(UserConstant.USER_LOGIN_CODE_KEY);
        // 获取redis中的验证码
        String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
        // 校验邮箱格式
        if (RegexUtils.isEmailInvalid(phone)) {
            return Result.fail("邮箱格式不正确");
        }
        // 校验验证码
        log.info("code:{}, cacheCode:{}", code, cacheCode);
        if(code == null || !cacheCode.toString().equals(code)){
            // 不一致则报错
            return Result.fail("验证码错误");
        }

        // 根据账号查询账户是否存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getPhone, phone);
        User user = this.getOne(queryWrapper);
        // 如果不存在则创建新用户
        if(user == null){
            user = createUerWithPhone(phone);
        }
//        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        // 将用户信息存入session
//        session.setAttribute(UserConstant.USER_LOGIN_CODE_KEY, userDTO);
        // 将用户信息存入redis
        // 随机生成token， 作为登录令牌
        String token = UUID.randomUUID().toString();
        // 将UserDTO对象转换为HashMap存储
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        HashMap<String,String> userMap = new HashMap<>();
        userMap.put("icon",userDTO.getIcon());
        userMap.put("id",String.valueOf(userDTO.getId()));
        userMap.put("nickName",userDTO.getNickName());

        // 存储
        String tokenKey = LOGIN_USER_KEY + token;
        stringRedisTemplate.opsForHash().putAll(tokenKey,userMap);
        // 设置token有效期为30分钟
        stringRedisTemplate.expire(tokenKey,30,TimeUnit.MINUTES);
        // 登录成功则删除验证码信息
        stringRedisTemplate.delete(LOGIN_CODE_KEY + phone);
        // 返回token
        return Result.ok(token);
    }
    private User createUerWithPhone(String phone){
        // 创建用户
        User user = new User();
        // 设置手机号
        user.setPhone(phone);
        // 设置昵称(默认名字),一个固定前缀 + 随机字符串
        user.setNickName("user_" + RandomUtil.randomString(10));
        // 保存到数据库
        this.save(user);
        return user;
    }
}
