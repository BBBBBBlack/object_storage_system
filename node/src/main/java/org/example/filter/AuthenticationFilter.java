package org.example.filter;

import org.example.mapper.BucketMapper;
import org.example.util.WebUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

//@Component
@Order(2)
@WebFilter(urlPatterns = {"/put/*", "/get/*", "/delete/*"}, filterName = "authenticationFilter")
public class AuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private BucketMapper bucketMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        boolean flag = false;
        String requestURI = request.getRequestURI();
        String bucketId = request.getParameter("bucketId");
        String userId = request.getHeader("id");
        if (requestURI.startsWith("/get/getBucket/")) {
            String[] uriParts = requestURI.split("/");
            bucketId = uriParts[3];
        }
        if (bucketId != null) {
            // 判断当前用户是否为该bucket的创建者
            Integer cnt = bucketMapper.isCreator(bucketId, userId);
            if (cnt == 0) {
                Integer acl = bucketMapper.getAcl(bucketId);
                if (acl != null) {
                    // 桶为公共读，且请求为get
                    if (acl == 1) {
                        if (requestURI.startsWith("/get")) {
                            flag = true;
                        }
                    }
                    // 桶为公有写
                    else if (acl == 2 || acl == 3) {
                        flag = true;
                    }
                    // 桶为其他权限
                    else if (acl == 4) {
                        List<Integer> permission = bucketMapper.getPermission(bucketId, userId);
                        if ((requestURI.startsWith("/put") || requestURI.startsWith("/delete")) && permission.contains(2)) {
                            flag = true;
                        } else if (requestURI.startsWith("/get") && permission.contains(0)) {
                            flag = true;
                        }
                    }
                }
            } else {
                flag = true;
            }
        } else {
            flag = true;
        }
        if (flag) {
            filterChain.doFilter(request, response);
        } else {
            WebUtil.writeResponse(response, 403, "没有权限");
        }
    }
}
