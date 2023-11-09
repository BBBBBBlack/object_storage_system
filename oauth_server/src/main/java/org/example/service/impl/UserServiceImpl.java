package org.example.service.impl;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import org.example.pojo.ResponseResult;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.*;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.endpoint.TokenEndpoint;
import org.springframework.stereotype.Service;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.client.RestTemplate;

import java.security.Principal;
import java.util.Map;

@Service
@Data
@ConfigurationProperties(prefix = "oauth-user-details")
//@PropertySource("classpath:application.yml")
public class UserServiceImpl implements UserService {
    @Autowired
    private RestTemplate restTemplate;

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
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        String token = JSONObject.parseObject(response.getBody()).get("access_token").toString();
        return new ResponseResult<>(200, "登录成功", token);
    }
}
