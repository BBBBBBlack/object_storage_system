package org.example.context;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.example.pojo.User;

import java.util.HashMap;
import java.util.Map;

@ToString
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserContextImpl implements UserContext {

    private static final long serialVersionUID = 8383356012441014698L;
    // 变量
    private Map<String, Object> properties = new HashMap<>();

    @Override
    public User getUser() {

        return null;
    }

    @Override
    public Object getProperty(String property) {
        return properties.get(property);
    }

    @Override
    public void setProperty(String property, Object value) {
        properties.put(property, value);
    }
}

