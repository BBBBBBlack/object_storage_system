package org.example.context;

import org.example.pojo.User;

import java.io.Serializable;

public interface UserContext extends Serializable {

    // 项目中定义的用户实体类
    User getUser();

    // 获取变量
    Object getProperty(String property);

    // 设置变量
    void setProperty(String property, Object value);

}

