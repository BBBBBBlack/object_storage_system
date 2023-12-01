package org.example.protocol.Message;

import io.netty.channel.DefaultFileRegion;

import java.io.File;
import java.io.Serializable;
import java.nio.channels.FileChannel;

public class DefaultRegion extends DefaultFileRegion implements Serializable {

    public DefaultRegion(FileChannel file, long position, long count) {
        super(file, position, count);
    }

    public DefaultRegion(File f, long position, long count) {
        super(f, position, count);
    }

    public Integer getType() {
        return Message.DefaultRegionRequestMessage;
    }

    public Integer getSequenceId() {
        return 1;
    }
}
