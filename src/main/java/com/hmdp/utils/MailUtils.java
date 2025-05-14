package com.hmdp.utils;

import com.hmdp.constant.MailContant;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Class name: MailUtils
 * Package: com.hmdp.utils
 * Description:
 * 该工具类实现发送邮件验证码
 *
 * @Create: 2025/5/14 0:18
 * @Author: jay
 * @Version: 1.0
 */
public class MailUtils {
    public static void main(String[] args) {
        sendTestMail("338272743@qq.com", achieveCode());
    }

    public static void sendTestMail(String mail, String code) {
        //创建Properties类用于记录邮箱的一些属性
        Properties props = new Properties();
        //表示SMTP发送邮件，必须进行身份验证
        props.put("mail.smtp.auth", "true");
        //填写SMTP服务器地址
        props.put("mail.smtp.host", "smtp.qq.com");
        //端口号，QQ邮箱的端口587
        props.put("mail.smtp.port", "587");
        //设置写信人的账号
        props.put("mail.user", "3383272743@qq.com");
        //设置16位的SMTP口令
        props.put("mail.password", MailContant.SMTP_AUTHORIZATION_CODE);
        //构建授权信息，用于进行SMTP身份验证
        Authenticator authenticator = new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                //发件人邮箱
                String userName = props.getProperty("mail.user");
                //发件人邮箱授权码
                String password = props.getProperty("mail.password");
                //发件人邮箱和授权码
                return new PasswordAuthentication(userName, password);
            }
        };
        //使用环境属性和授权信息，创建邮件会话
        Session mailSession = Session.getInstance(props, authenticator);
        //创建邮件消息
        MimeMessage message = new MimeMessage(mailSession);
        // 设置发件人
        try {
            InternetAddress form = new InternetAddress(props.getProperty("mail.user"));
            message.setFrom(form);
            //设置收件人
            InternetAddress to = new InternetAddress("3383272743@qq.com");
            message.setRecipient(MimeMessage.RecipientType.TO, to);
            //设置邮件标题
            message.setSubject("邮件测试");
            //设置邮件内容
            message.setContent("您的验证码是：" + code + "(有效期为一分钟，请勿告诉他人)", "text/html;charset=UTF-8");
            //发送邮件
            Transport.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String achieveCode() {  //由于数字 1 、 0 和字母 O 、l 有时分不清楚，所以，没有数字 1 、 0
        String[] beforeShuffle = new String[]{"2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F",
                "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "a",
                "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v",
                "w", "x", "y", "z"};
        List<String> list = Arrays.asList(beforeShuffle);
        //将数组转换为集合
        Collections.shuffle(list);
        //打乱集合顺序
        StringBuilder sb = new StringBuilder();
        for (String s : list) {
            sb.append(s);
            //将集合转化为字符串
        }
        return sb.substring(3, 8);
    }
}

