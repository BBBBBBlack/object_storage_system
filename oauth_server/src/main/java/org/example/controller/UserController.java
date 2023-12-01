package org.example.controller;

import org.example.pojo.ResponseResult;
import org.example.pojo.User;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class UserController {

    @Autowired
    private UserService userService;

    @RequestMapping("/login")
    public ResponseResult<String> login(@RequestBody Map<String, String> parameters) {
        return userService.login(parameters);
    }

    @PostMapping("/register")
    public ResponseResult register(@RequestParam String userEmail,
                                   @RequestParam String password,
                                   @RequestParam(required = false) Integer type,
                                   @RequestParam String code) {
        User user = new User();
        user.setUserEmail(userEmail);
        user.setPassword(password);
        user.setType(type);
        return userService.register(user, code);
    }

    @GetMapping("/get_user_info")
    public ResponseResult getUserInfo(@RequestHeader(required = false) String userEmail,
                                      @RequestHeader(required = false) String nickName,
                                      @RequestHeader(required = false) String phoneNumber,
                                      @RequestHeader(required = false) String picture) {
        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("userEmail", userEmail);
        userInfo.put("nickName", nickName);
        userInfo.put("phoneNumber", phoneNumber);
        userInfo.put("picture", picture);
        return new ResponseResult(200, "获取用户信息", userInfo);
    }

    @PostMapping("/fill_user_message")
    public ResponseResult fillUserMessage(@RequestHeader String id,
                                          @RequestHeader(required = false) String picture,
                                          @RequestParam(required = false) String password,
                                          @RequestParam(required = false) String nickName,
                                          @RequestParam(required = false) MultipartFile headImg) {
        User user = new User();
        user.setId(Long.parseLong(id));
        if (headImg != null && !headImg.isEmpty()){
            user.setPicture(picture);
        }
        user.setPassword(password);
        user.setNickName(nickName);
        return userService.fillUserMessage(user, headImg);
    }


    @RequestMapping("/getCurrentUser")
    public Object getCurrentUser(Authentication authentication) {
        return authentication.getPrincipal();
    }
}
