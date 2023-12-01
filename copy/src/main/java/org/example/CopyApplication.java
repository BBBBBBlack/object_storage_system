package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CopyApplication {
    public static void main(String[] args) {
        SpringApplication.run(CopyApplication.class, args);
    }
}