package org.example.protocol.Message;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class WriteRequestMessage extends Message implements Serializable {

    private String bucketId;

    private String fileName;

    private Long totalNum;//文件总片数

    private Integer sequenceId;

    private byte[] content;

    private Integer version;

    public WriteRequestMessage(String bucketId, String fileName, Long totalNum, Integer sequenceId, Integer version) {
        this.bucketId = bucketId;
        this.fileName = fileName;
        this.totalNum = totalNum;
        this.sequenceId = sequenceId;
        this.version = version;
//        content = new byte[TCPProperty.maxSend];
    }

    @Override
    public Integer getType() {
        return Message.WriteRequestMessage;
    }

}
