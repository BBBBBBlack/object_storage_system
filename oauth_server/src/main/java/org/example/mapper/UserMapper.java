package org.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.pojo.User;

@Mapper
public interface UserMapper {
    User getUserByName(String userEmail);

    void addUser(User user);

    void updateUserById(User user);
}
