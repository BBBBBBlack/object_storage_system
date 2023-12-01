package org.example.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * (Bucket)实体类
 *
 * @author bbbbbblack
 * @since 2023-11-09 21:07:09
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bucket implements Serializable {

    private static final long serialVersionUID = 554831041630869539L;

    private Integer id;

    private String name;
    /**
     * 创建者
     */
    private String creater;
    /**
     * 访问控制列表：
     * 0-private;
     * 1-public-read;
     * 2-public-read-write;
     * 3-authenticated-read;
     * 4-else
     */
    private Integer acl;
    /**
     * 对象锁？
     */
    private Integer lockEnable;


}

