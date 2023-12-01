package org.example.service.impl;

import org.example.mapper.UserMapper;
import org.example.pojo.Email;
import org.example.pojo.ResponseResult;
import org.example.service.EmailService;
import org.example.util.EmailUtil;
import org.example.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Value("${spring.mail.username}")
    String from;

    @Override
    public ResponseResult sendMail(String email) {
        String code = UUID.randomUUID().toString();
        Email mail = new Email(from, email, null, code);
        if (userMapper.getUserByName(email) != null) {
            return new ResponseResult(200, "用户已存在");
        }
        mail.setSubject("注册邮件：请复制以下验证码（30分钟内有效）\n");
        redisUtil.setCacheObject(mail.getTo(), code, 30, TimeUnit.MINUTES);
        EmailUtil.send(mail);
        return new ResponseResult(200, "邮件发送成功");
    }
}

