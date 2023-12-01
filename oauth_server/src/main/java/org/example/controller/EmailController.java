package org.example.controller;

import org.example.pojo.ResponseResult;
import org.example.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/email")
public class EmailController {
    @Autowired
    private EmailService mailService;

    @PostMapping("/send_email")
    public ResponseResult sendMail(@RequestParam String email) {
        return mailService.sendMail(email);
    }
}
