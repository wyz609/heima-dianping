package com.hmdp.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.constant.UserConstant;
import com.hmdp.dto.Result;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

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

    /**
     * 发送手机验证码
     *
     * @param phone   手机号
     * @param session 会话
     * @return 结果
     */
    @Override
    public Result sendCode(String phone, HttpSession session) {
        //1.校验手机号码
        // 校验方法一
//        if(phone == null || !phone.matches("1[3-9]\\d{9}")){
//            return Result.fail("手机号格式错误");
//        }
        // 校验方法二
        if(RegexUtils.isPhoneInvalid(phone)){
            return Result.fail("手机号格式错误");
        }
        //2.生成验证码
        String code = RandomUtil.randomNumbers(6);
        //3.将验证码保存到Session中
        session.setAttribute(UserConstant.USER_LOGIN_CODE_KEY, code);

        log.info("发送验证码成功，验证码：{}", code);
        // TODO 调用阿里云，将短信信息发送到指定手机
        return Result.ok();
    }
}
