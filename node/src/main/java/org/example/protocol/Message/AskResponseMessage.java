package org.example.protocol.Message;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;

@Data
@NoArgsConstructor
//返回服务端文件信息
public class AskResponseMessage extends Message implements Serializable {

    private String bucketId;

    private String fileName;

    private Set<Integer> fileSet;//服务端存在的文件分片集合

    private Integer version;

    public AskResponseMessage(String bucketId, String fileName, Integer version) {
        this.bucketId = bucketId;
        this.fileName = fileName;
        this.version = version;
    }

    @Override
    public Integer getType() {
        return Message.AskResponseMessage;
    }

    @Override
    public Integer getSequenceId() {
        return -1;
    }
}
