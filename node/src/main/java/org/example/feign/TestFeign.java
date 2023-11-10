package org.example.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;

@FeignClient(name = "node01-service", url = "EMPTY")
public interface TestFeign {

    @GetMapping("/bucket/test")
    String test(URI uri, @RequestParam String str);

}
