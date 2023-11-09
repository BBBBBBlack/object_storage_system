package org.example.service;

import org.example.pojo.ResponseResult;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.Map;

public interface UserService {
    ResponseResult<String> login(Map<String, String> parameters);
}
