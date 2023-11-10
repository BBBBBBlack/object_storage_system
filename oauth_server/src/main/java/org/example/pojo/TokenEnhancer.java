package org.example.pojo;

import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import java.util.HashMap;
import java.util.Map;

//@Component
public class TokenEnhancer implements org.springframework.security.oauth2.provider.token.TokenEnhancer {


    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {

        LoginUser loginUser = (LoginUser) authentication.getPrincipal();

        final Map<String, Object> additionalInfo = new HashMap<>();

        final Map<String, Object> retMap = new HashMap<>();

        //todo 可以根据自己的业务需要 进行添加字段
        additionalInfo.put("id", loginUser.getUser().getId());

        additionalInfo.put("nickName", loginUser.getUser().getNickName());

        additionalInfo.put("phoneNumber", loginUser.getUser().getPhoneNumber());

        additionalInfo.put("picture", loginUser.getUser().getPicture());

        additionalInfo.put("userStatus", loginUser.getUser().getUserStatus());

        additionalInfo.put("type", loginUser.getUser().getType());

        retMap.put("additionalInfo", additionalInfo);

        ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(retMap);

        return accessToken;
    }
}