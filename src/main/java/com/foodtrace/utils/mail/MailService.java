package com.foodtrace.utils.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import java.io.File;
import java.util.List;

/**
 * 邮箱 验证码 服务类
 */
@Service
public class MailService {
    @Value("${spring.mail.username}")
    private String from;
    @Autowired
    private JavaMailSender mailSender;

    private static final String CONTENT_PREFIX = "您的验证码为 ";
    private static final String CONTENT_SUFFIX = " ,有效时间 5 分钟, 请勿泄露!";

    Logger logger = LoggerFactory.getLogger(this.getClass());

    public boolean sendSimpleMail(String to,String title,String content){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(title);
        message.setText(CONTENT_PREFIX + content + CONTENT_SUFFIX);
        mailSender.send(message);
        logger.info("邮件发送成功");
        return true;
    }

    public boolean sendAttachmentsMail(String to, String title, String cotent, List<File> fileList){
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message,true);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(title);
            helper.setText(cotent);
            String fileName;
            for (File file:fileList) {
                fileName = MimeUtility.encodeText(file.getName(), "GB2312", "B");
                helper.addAttachment(fileName, file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mailSender.send(message);
        logger.info("邮件发送成功");
        return true;
    }
}