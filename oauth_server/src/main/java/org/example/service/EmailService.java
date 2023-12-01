package org.example.service;


import org.example.pojo.ResponseResult;

public interface EmailService {
    ResponseResult sendMail(String email);
}
