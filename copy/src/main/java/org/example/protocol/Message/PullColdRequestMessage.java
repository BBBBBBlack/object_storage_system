package org.example.protocol.Message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PullColdRequestMessage extends Message implements Serializable {

    private String bucketId;

    private String fileName;

    @Override
    public Integer getType() {
        return PullColdRequestMessage;
    }

    @Override
    public Integer getSequenceId() {
        return -1;
    }
}
