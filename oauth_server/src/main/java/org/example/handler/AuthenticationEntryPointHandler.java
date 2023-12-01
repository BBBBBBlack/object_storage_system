package org.example.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.pojo.ResponseResult;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.resource.OAuth2AccessDeniedException;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.error.OAuth2AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Component
public class AuthenticationEntryPointHandler extends OAuth2AuthenticationEntryPoint {


    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {

        Throwable cause = authException.getCause();
        ResponseResult res = new ResponseResult();
        if (cause instanceof InvalidTokenException) {
            res.setMsg("Token解析失败");
        } else if (authException instanceof InsufficientAuthenticationException) {
            res.setMsg("未携带token");
        } else {
            res.setMsg("未知异常信息");
        }

        response.setStatus(HttpStatus.OK.value());
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Type", "application/json;charset=UTF-8");
        try {
            PrintWriter printWriter = response.getWriter();
            printWriter.append(new ObjectMapper().writeValueAsString(res));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
