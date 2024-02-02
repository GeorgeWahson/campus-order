package com.watson.order.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import javax.mail.internet.InternetAddress;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

@Slf4j
@RequiredArgsConstructor
@Component
public class SendMailUtils {

    //发送人
    @Value("${MAIL_USER}")
    private String from;

    private final JavaMailSender javaMailSender;

    public void sendSimpleMail(String code, String userEmail) {

        //发送人昵称
        String nickName = "【校园点餐】";
        //标题
        String subject = "校园点餐平台登录验证码";
        //正文
        String simple_context = "您的验证码是：【" + code + "】，验证码五分钟内有效。\n 若非您本人操作，请忽略该邮件。";

        SimpleMailMessage message = new SimpleMailMessage();

//        message.setFrom(from);  // 不带昵称
        try {
            message.setFrom(String.valueOf(new InternetAddress(from, nickName, "UTF-8")));  // 带昵称
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage());
        }
        // 设置接收人
        message.setTo(userEmail);

        message.setSubject(subject);

        message.setText(simple_context);

        javaMailSender.send(message);

        log.info("已发送一次【普通】邮件给：{}, 验证码为： {}", Arrays.toString(message.getTo()), code);
    }
}
