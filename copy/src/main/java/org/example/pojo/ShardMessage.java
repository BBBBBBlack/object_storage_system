package org.example.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShardMessage {
    private static Map<String, ShardMessage> shardMap = new HashMap<>();
    private String fileName;
    private Integer shardNum;
    private Long shardSize;
    private String bucketId;
    private Boolean isZip;
    private Set<Integer> shardSet;//分片集
    private Integer version;

    public static Map<String, ShardMessage> getInstance() {
        return shardMap;
    }
}
