package org.example.pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * (User)实体类
 *
 * @author bbbbbblack
 * @since 2023-11-09 17:22:16
 */
@Data
public class User implements Serializable {
    private static final long serialVersionUID = -40129501617115392L;
    /**
     * 主键——用户id
     */
    private Long id;
    /**
     * 用户邮箱（充当用户名user_name）
     */
    private String userEmail;
    /**
     * 用户昵称
     */
    private String nickName;
    /**
     * 用户密码
     */
    private String password;
    /**
     * 用户电话
     */
    private String phoneNumber;
    /**
     * 用户头像
     */
    private String picture;
    /**
     * 用户状态——正常状态为1，封禁状态为0
     */
    private Integer userStatus;
    /**
     * 用户类型——1为普通用户，2为商户
     */
    private Integer type;

}

