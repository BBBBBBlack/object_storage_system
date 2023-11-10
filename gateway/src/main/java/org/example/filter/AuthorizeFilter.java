package org.example.filter;

import com.alibaba.cloud.nacos.discovery.NacosDiscoveryClient;
import com.alibaba.fastjson.JSON;
import io.jsonwebtoken.Claims;
import org.example.property.NotAuthUrlProperties;
import org.example.util.JwtUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.PublicKey;
import java.util.Map;

@Order(-1)
@Component
@EnableConfigurationProperties(value = NotAuthUrlProperties.class)
public class AuthorizeFilter implements GlobalFilter {

    @Autowired
    DiscoveryClient discoveryClient;
    /**
     * jwt的公钥,需要网关启动,远程调用认证中心去获取公钥
     */
    private PublicKey publicKey;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 请求各个微服务 不需要用户认证的URL
     */
    @Autowired
    private NotAuthUrlProperties notAuthUrlProperties;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //1.过滤不需要认证的url,比如/oauth/**
        String currentUrl = exchange.getRequest().getURI().getPath();

        //过滤不需要认证的url
        if (shouldSkip(currentUrl)) {
            return chain.filter(exchange);
        }

        //2. 获取token
        // 从请求头中解析 Authorization  value:  bearer xxxxxxx
        // 或者从请求参数中解析 access_token
        //第一步:解析出我们Authorization的请求头  value为: “bearer XXXXXXXXXXXXXX”
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

        //第二步:判断Authorization的请求头是否为空
        if (StringUtils.isEmpty(authHeader)) {
            throw new NullPointerException("需要认证的url,请求头为空");
        }

        //3. 校验token
        // 拿到token后，通过公钥（需要从授权服务获取公钥）校验
        // 校验失败或超时抛出异常
        //第三步 校验我们的jwt 若jwt不对或者超时都会抛出异常
        Claims claims = null;
        try {
            if (this.publicKey == null) {
                this.publicKey = jwtUtils.genPublicKey(restTemplate);
            }
            claims = jwtUtils.validateJwtToken(authHeader, publicKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //4. 校验通过后，从token中获取的用户登录信息存储到请求头中
        //第四步 把从jwt中解析出来的 用户登陆信息存储到请求头中
        ServerWebExchange webExchange = wrapHeader(exchange, claims);

        return chain.filter(webExchange);
    }

    private ServerWebExchange wrapHeader(ServerWebExchange serverWebExchange, Claims claims) {

        String userId = claims.get("additionalInfo", Map.class).get("id").toString();

        String nickName = claims.get("additionalInfo", Map.class).get("nickName").toString();

        String picture = claims.get("additionalInfo", Map.class).get("picture").toString();

        String phoneNumber = claims.get("additionalInfo", Map.class).get("phoneNumber").toString();

        String userStatus = claims.get("additionalInfo", Map.class)
                .get("userStatus").toString();

        String type = claims.get("additionalInfo", Map.class)
                .get("type").toString();

        //向headers中放文件，记得build
        ServerHttpRequest request = serverWebExchange.getRequest().mutate()
                .header("userEmail", claims.get("user_name", String.class))
                .header("id", userId)
                .header("nickName", nickName)
                .header("picture", picture)
                .header("phoneNumber", phoneNumber)
                .header("userStatus", userStatus)
                .header("type", type)
                .build();

        //将现在的request 变成 change对象
        return serverWebExchange.mutate().request(request).build();
    }

    private boolean shouldSkip(String currentUrl) {
        //路径匹配器(简介SpringMvc拦截器的匹配器)
        //比如/oauth/** 可以匹配/oauth/token    /oauth/check_token等
        PathMatcher pathMatcher = new AntPathMatcher();
        for (String skipPath : notAuthUrlProperties.getShouldSkipUrls()) {
            if (pathMatcher.match(skipPath, currentUrl)) {
                return true;
            }
        }
        return false;
    }

}

