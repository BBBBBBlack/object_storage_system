package org.example.filter;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.example.context.UserContextHolder;
import org.springframework.stereotype.Component;

@Component
public class FeignRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {
        requestTemplate.header("id",
                UserContextHolder.getContext().getProperty("id").toString());
        requestTemplate.header("userEmail",
                UserContextHolder.getContext().getProperty("userEmail").toString());
        requestTemplate.header("nickName",
                UserContextHolder.getContext().getProperty("nickName").toString());
        requestTemplate.header("phoneNumber",
                UserContextHolder.getContext().getProperty("phoneNumber").toString());
        requestTemplate.header("picture",
                UserContextHolder.getContext().getProperty("picture").toString());
        requestTemplate.header("userStatus",
                UserContextHolder.getContext().getProperty("userStatus").toString());
        requestTemplate.header("type",
                UserContextHolder.getContext().getProperty("type").toString());
    }
}

