package org.example.util;

import org.example.cache.BucketCache;
import org.example.protocol.Client;
import org.example.protocol.Message.PullRequestMessage;
import org.example.protocol.factory.ClientFactory;
import org.example.property.FileProperty;
import org.example.protocol.Message.AskRequestMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class CopyUtil {

    @Autowired
    private BucketCache bucketCache;

    @Autowired
    private ClientFactory clientFactory;

    public void autoCopy(String copyIp, Integer copyPort) {
        clientFactory.createClient(copyIp, copyPort);
        //获取此节点所有bucketId
        List<String> bucketIdList = new ArrayList<>();
        File file = new File(FileProperty.realPath);
        File[] tempList = file.listFiles();
        for (File value : tempList) {
            if (value.isDirectory()) {
                bucketIdList.add(value.getName());
            }
        }
        for (String bucketId : bucketIdList) {
            Map<String, Integer> nvMap = bucketCache.getFileSetByName(bucketId, 1);
            Set<String> keySet = nvMap.keySet();
            for (String filePath : keySet) {
                Integer version = nvMap.get(filePath);
                String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
                AskRequestMessage message = new AskRequestMessage(bucketId, fileName, version);
                TCPUtil.autoCopy(copyIp, copyPort, message);
            }
        }

    }

    public void pull(String copyIp, Integer copyPort, String bucketId) {
        clientFactory.createClient(copyIp, copyPort);
        Client.sendMessage(copyIp, copyPort, new PullRequestMessage(bucketId));
    }
}
