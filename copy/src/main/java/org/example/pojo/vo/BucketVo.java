package org.example.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.pojo.Bucket;

import java.io.Serializable;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BucketVo implements Serializable {
    private static final long serialVersionUID = 38756139649234789L;

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

    private Set<String> fileSet;//文件名列表

    public BucketVo(Integer id){
        this.id = id;
    }

    public void setBucket(Bucket bucket){
        this.id = bucket.getId();
        this.name = bucket.getName();
        this.creater = bucket.getCreater();
        this.acl = bucket.getAcl();
        this.lockEnable = bucket.getLockEnable();
    }
}
