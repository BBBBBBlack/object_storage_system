package org.example.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.pojo.ResponseResult;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.IOException;

public class WebUtils {
    public static Mono<Void> writeResponse(ServerHttpResponse response, Integer code, String message) {
        response.getHeaders().
                add("Content-Type", "application/json;charset=UTF-8");
        DataBufferFactory bufferFactory = response.bufferFactory();
        ObjectMapper objectMapper = new ObjectMapper();
        DataBuffer wrap = null;
        try {
            wrap = bufferFactory.wrap(objectMapper
                    .writeValueAsBytes(new ResponseResult<>(code, message)));
            DataBuffer finalWrap = wrap;
            return response.writeWith(Mono.fromSupplier(() -> finalWrap));
        } catch (
                JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}