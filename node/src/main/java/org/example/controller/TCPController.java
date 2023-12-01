package org.example.controller;

import org.example.util.FileUtil;
import org.example.util.CopyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/tcp")
public class TCPController {

    @Autowired
    private CopyUtil copyUtil;

    @Value("${tcp.auto-copy.copy-ip}")
    private String copyIp;

    @Value("${tcp.auto-copy.copy-port}")
    private Integer copyPort;

    @RequestMapping("/test")
    public String getMd5(@RequestParam MultipartFile file) {
        return FileUtil.getMd5(file);
    }

    @RequestMapping("/copy")
    public void copy() {
        copyUtil.autoCopy(copyIp, copyPort);
    }

    @RequestMapping("/pull")
    public void pull(@RequestParam String bucketId) {
        copyUtil.pull(copyIp, copyPort, bucketId);
    }

}
