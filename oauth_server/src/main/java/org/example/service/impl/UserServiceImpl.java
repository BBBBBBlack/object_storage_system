package org.example.service.impl;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import org.example.mapper.UserMapper;
import org.example.pojo.ResponseResult;
import org.example.pojo.User;
import org.example.service.UserService;
import org.example.util.FileUtil;
import org.example.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.endpoint.TokenEndpoint;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Data
@ConfigurationProperties(prefix = "oauth-user-details")
//@PropertySource("classpath:application.yml.b")
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    @Qualifier("jwtTokenStore")
    private TokenStore tokenStore;

    private String client_id;

    private String client_secret;

    private String grant_type;

    private String scope;

    @Override
    public ResponseResult<String> login(Map<String, String> parameters) {
        String url = "http://oauth-service/oauth/token?" +
                "username=" + parameters.get("username") +
                "&password=" + parameters.get("password") +
                "&grant_type=" + grant_type +
                "&client_id=" + client_id +
                "&client_secret=" + client_secret +
                "&scope=" + scope;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        } catch (HttpServerErrorException e) {
            return new ResponseResult<>(200, "用户名错误");
        } catch (HttpClientErrorException e) {
            return new ResponseResult<>(200, "密码错误");
        }
        String access_token = JSONObject.parseObject(response.getBody()).get("access_token").toString();
        return new ResponseResult<>(200, "登录成功", access_token);
    }

    @Override
    public ResponseResult register(User user, String code) {
        if (userMapper.getUserByName(user.getUserEmail()) != null) {
            return new ResponseResult(200, "用户已存在");
        }
        String cc = redisUtil.getCacheObject(user.getUserEmail());
        if (code.equals(cc)) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setNickName(UUID.randomUUID().toString());
            userMapper.addUser(user);
//            roleMapper.addRole(user.getId(), user.getType().longValue());
            redisUtil.deleteObject(user.getUserEmail());
            return new ResponseResult(200, "注册成功，请重新登录");
        }
        return new ResponseResult(200, "验证码错误");
    }


    @Override
    public ResponseResult fillUserMessage(User user, MultipartFile headImg) {
        //修改密码
        if (StringUtils.hasText(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        //添加头像
        if (!headImg.isEmpty()) {
            String headUrl = null;
            try {
                headUrl = FileUtil.upLoadProImag(headImg);
            } catch (Exception e) {
                return new ResponseResult(500, e.getMessage());
            }
            //删去旧头像
            String oldUrl = user.getPicture();
            if (StringUtils.hasText(oldUrl)) {
                FileUtil.delete(oldUrl);
            }
            user.setPicture(headUrl);
        }
        userMapper.updateUserById(user);
        return new ResponseResult(200, "修改信息成功");
    }

}
