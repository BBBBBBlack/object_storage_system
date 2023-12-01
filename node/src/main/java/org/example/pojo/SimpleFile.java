package org.example.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimpleFile {
    private MultipartFile multipartFile;
    private String originMD5;
    private String bucketId;
    private Boolean isZip;
}
