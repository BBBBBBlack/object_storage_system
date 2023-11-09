package org.example.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashSet;

@Data
@ConfigurationProperties("gateway")
public class NotAuthUrlProperties {

    private LinkedHashSet<String> shouldSkipUrls;
}