package org.example.filter;

import org.example.context.UserContext;
import org.example.context.UserContextHolder;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;

@Component
public class FeignRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {
        UserContext userContext = UserContextHolder.getContext();
        String id = userContext.getProperty("id") == null ? null :
                userContext.getProperty("id").toString();
        String userEmail = userContext.getProperty("userEmail") == null ? null :
                userContext.getProperty("userEmail").toString();
        String nickName = userContext.getProperty("nickName") == null ? null :
                userContext.getProperty("nickName").toString();
        String phoneNumber = userContext.getProperty("phoneNumber") == null ? null :
                userContext.getProperty("phoneNumber").toString();
        String picture = userContext.getProperty("picture") == null ? null :
                userContext.getProperty("picture").toString();
        String userStatus = userContext.getProperty("userStatus") == null ? null :
                userContext.getProperty("userStatus").toString();
        String type = userContext.getProperty("type") == null ? null :
                userContext.getProperty("type").toString();
        requestTemplate.header("id", id);
        requestTemplate.header("userEmail", userEmail);
        requestTemplate.header("nickName", nickName);
        requestTemplate.header("phoneNumber", phoneNumber);
        requestTemplate.header("picture", picture);
        requestTemplate.header("userStatus", userStatus);
        requestTemplate.header("type", type);
    }
}

