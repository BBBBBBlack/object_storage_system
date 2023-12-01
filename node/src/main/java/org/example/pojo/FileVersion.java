package org.example.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Data
@NoArgsConstructor
//@AllArgsConstructor
public class FileVersion implements Serializable {
    public static Map<String, AtomicLong> fvMap = new HashMap<>();

    public static Long addVersion(String key) {
        if (!fvMap.containsKey(key)) {
            synchronized (FileVersion.class) {
                if (!fvMap.containsKey(key)) {
                    fvMap.put(key, new AtomicLong(-1));
                }
            }
        }
        return fvMap.get(key).incrementAndGet();
    }

    public static Long getVersion(String key) {
        return fvMap.get(key).get();
    }
}
