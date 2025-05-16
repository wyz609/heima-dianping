package com.hmdp.utils;

import com.hmdp.constant.MailConstant;

import javax.mail.*;
import javax.mail.internet.*;
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
 * @Version: 1.2
 */
public class MailUtils {

    /**
     * 发送邮件
     *
     * @param mail 收件人邮箱
     * @param code 验证码
     * @throws MessagingException 如果发送邮件失败
     */
    public static void sendTestMail(String mail, String code) throws MessagingException {
        // 创建 Properties 类用于记录邮箱属性
        Properties props = getProperties();

        // 设置发件人邮箱和授权码
        String userName = "3383272743@qq.com"; // 发件人邮箱
        String password = MailConstant.SMTP_AUTHORIZATION_CODE; // 授权码
        // 构建授权信息
        Authenticator authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(userName, password);
            }
        };

        // 创建邮件会话
        Session mailSession = Session.getInstance(props, authenticator);
        // 启用调试日志，便于排查问题
        mailSession.setDebug(true);

        // 创建邮件消息
        MimeMessage message = new MimeMessage(mailSession);
        // 设置发件人
        message.setFrom(new InternetAddress(userName));
        // 设置收件人
        message.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(mail));
        // 设置邮件标题
        message.setSubject("邮箱验证码");
        // 设置邮件内容
        message.setContent("您的验证码是：<b>" + code + "</b> (有效期为一分钟，请勿告诉他人)", "text/html;charset=UTF-8");

        // 发送邮件
        Transport.send(message);
    }

    private static Properties getProperties() {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.qq.com"); // QQ 邮箱 SMTP 服务器
        props.put("mail.smtp.port", "465"); // SSL 端口
        props.put("mail.smtp.auth", "true"); // 启用认证
        props.put("mail.smtp.ssl.enable", "true"); // 启用 SSL
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory"); // 使用 SSL Socket
        props.put("mail.smtp.socketFactory.port", "465"); // 设置 SSL 端口
        props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3"); // 显式指定 TLS 协议
        return props;
    }

    /**
     * 生成 6 位随机验证码（不包含易混淆字符 0, 1, O, l）
     *
     * @return 6 位验证码
     */
    public static String achieveCode() {
        String[] chars = new String[]{"2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F",
                "G", "H", "I", "J", "K", "L", "M", "N", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
                "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "m", "n", "p", "q", "r", "s", "t", "u", "v",
                "w", "x", "y", "z"};
        List<String> list = Arrays.asList(chars);
        Collections.shuffle(list); // 随机打乱
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(list.get(i)); // 取前 6 位
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        try {
            String code = achieveCode();
            sendTestMail("3383272743@qq.com", code);
            System.out.println("邮件发送成功，验证码: " + code);
        } catch (MessagingException e) {
            System.err.println("邮件发送失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}