package org.example.protocol.Message;

import lombok.Data;

import java.io.Serializable;

@Data
public class PullRequestMessage extends Message implements Serializable {

    private String bucketId;

    @Override
    public Integer getType() {
        return PullRequestMessage;
    }

    public PullRequestMessage(String bucketId) {
        this.bucketId = bucketId;
    }

    @Override
    public Integer getSequenceId() {
        return 1;
    }
}
