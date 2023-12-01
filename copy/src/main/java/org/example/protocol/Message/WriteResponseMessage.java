package org.example.protocol.Message;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class WriteResponseMessage extends Message implements Serializable {

    private String bucketId;

    private String fileName;

    private Integer version;

    public WriteResponseMessage(String bucketId, String fileName, Integer version) {
        this.bucketId = bucketId;
        this.fileName = fileName;
        this.version = version;
    }

    @Override
    public Integer getType() {
        return WriteResponseMessage;
    }

    @Override
    public Integer getSequenceId() {
        return 1;
    }
}
