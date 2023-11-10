package org.example.filter;

import org.example.context.UserContext;
import org.example.context.UserContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@WebFilter(urlPatterns = "/*", filterName = "getContextFilter")
public class GetContextFilter extends OncePerRequestFilter {


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        UserContext userContext = UserContextHolder.getContext();

        userContext.setProperty("id", request.getHeader("id"));

        userContext.setProperty("userEmail", request.getHeader("userEmail"));

        userContext.setProperty("nickName", request.getHeader("nickName"));

        userContext.setProperty("phoneNumber", request.getHeader("phoneNumber"));

        userContext.setProperty("picture", request.getHeader("picture"));

        userContext.setProperty("userStatus", request.getHeader("userStatus"));

        userContext.setProperty("type", request.getHeader("type"));

        filterChain.doFilter(request, response);
    }
}