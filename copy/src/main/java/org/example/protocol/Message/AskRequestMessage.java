package org.example.protocol.Message;


import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.property.FileProperty;
import org.example.util.FileUtil;

import java.io.File;
import java.io.Serializable;

@Data
@NoArgsConstructor
//询问服务端文件字节数
public class AskRequestMessage extends Message implements Serializable {

    private String bucketId;

    private String fileName;

    private Long totalBytes;//文件总字节数

    private String md5;

    private Integer version;

    public AskRequestMessage(String bucketId, String fileName, Integer version) {
        this.bucketId = bucketId;
        this.fileName = fileName;
        this.version = version;
    }

    public boolean complete() {
        String path = FileProperty.copyPath + bucketId + "/" + fileName + "/" + version;
        File file = new File(path);
        if (file.exists()) {
            totalBytes = file.length();
            md5 = FileUtil.getMd5(path);
            return true;
        }
        return false;
    }

    @Override
    public Integer getType() {
        return AskRequestMessage;
    }

    @Override
    public Integer getSequenceId() {
        return 1;
    }
}
