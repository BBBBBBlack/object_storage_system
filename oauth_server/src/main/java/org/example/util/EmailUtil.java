package org.example.util;

import lombok.Data;
import org.example.pojo.Email;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Data
@Component
public class EmailUtil {

    private static JavaMailSender javaMailSender;

    @Autowired
    public void setJavaMailSender(JavaMailSender javaMailSender) {
        EmailUtil.javaMailSender = javaMailSender;
    }

    public static void send(Email mail) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject(mail.getSubject());
        message.setText(mail.getText());
        message.setFrom(mail.getFrom());
        message.setTo(mail.getTo());
        javaMailSender.send(message);
    }
}
