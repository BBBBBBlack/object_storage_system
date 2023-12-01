package org.example.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.pojo.ResponseResult;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Mono;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class WebUtil {
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

    public static void writeResponse(HttpServletResponse response, Integer code, String message) {
        ResponseResult<String> responseResult = new ResponseResult<>(code, message);
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonResponse = null;
        try {
            jsonResponse = objectMapper.writeValueAsString(responseResult);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try {
            response.getWriter().write(jsonResponse);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}