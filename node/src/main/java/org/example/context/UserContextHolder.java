package org.example.context;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserContextHolder {

    // 创建一个线程变量
    private static final ThreadLocal<UserContext> contextHolder = new ThreadLocal<>();

    // 设置变量
    public static void setContext(UserContext userContext) {
        contextHolder.set(userContext);
    }

    public static UserContext getContext() {
        UserContext obj = contextHolder.get();
        if (obj == null) {
            obj = new UserContextImpl();
            setContext(obj);
        }
        return obj;
    }
}

