package org.example.service;

import org.example.pojo.ResponseResult;
import org.example.pojo.User;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.Map;

public interface UserService {
    ResponseResult<String> login(Map<String, String> parameters);

    ResponseResult register(User user, String code);

    ResponseResult fillUserMessage(User user, MultipartFile headImg);
}
