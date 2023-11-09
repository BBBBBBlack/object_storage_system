package org.example.service;

import org.example.mapper.UserMapper;
import org.example.pojo.LoginUser;
import org.example.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserDetailService implements UserDetailsService {
    @Autowired
    private UserMapper userMapper;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {


        // 加载用户信息
        if (StringUtils.isEmpty(username)) {
            //log.warn("用户登陆用户名为空:{}", username);
            throw new UsernameNotFoundException("用户名不能为空");
        }

        User user = userMapper.getUserByName(username);

        // 会员信息的封装 implements UserDetails

        return new LoginUser(user,null);
    }
}