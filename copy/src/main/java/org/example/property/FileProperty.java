package org.example.property;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FileProperty {
    public static Integer apiPort;//接口开放端口
    public static String realPath;
    public static String tempPath;
    public static String copyPath;
    public static String delPath;

    @Value("${server.port}")
    public void setApiPort(Integer apiPort) {
        FileProperty.apiPort = apiPort;
    }

    @Value("${base-url.windows}")
    public void setRealPath(String realPath) {
        FileProperty.realPath = (realPath + "realPath\\")
                .replace("\\", "/");
    }

    @Value("${base-url.windows}")
    public void setTempPath(String tempPath) {
        FileProperty.tempPath = tempPath + "tempPath\\"
                .replace("\\", "/");
    }

    @Value("${base-url.windows}")
    public void setCopyPath(String copyPath) {
        FileProperty.copyPath = copyPath + "copyPath\\"
                .replace("\\", "/");
    }

    @Value("${base-url.windows}")
    public void setDelPath(String delPath) {
        FileProperty.delPath = delPath + "delPath\\"
                .replace("\\", "/");
    }

}
