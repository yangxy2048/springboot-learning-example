package org.spring.springboot.keygenerator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.Map;

/**
 *
 * @author DevinYang
 * @since 2019-12-10
 * © All Rights Reserved.
 */
@Service
public class RedisCacheKeyGenerator implements KeyGenerator {

    @Override
    public Object generate(Object targetClass, Method method, Object... params) {
        Map map = Maps.newHashMap();
        Class<?> targetClassClass = targetClass.getClass();
        // 类地址
        map.put("class",targetClassClass.toGenericString());
        // 方法名称
        map.put("methodName",method.getName());
        // 包名称
        map.put("package",targetClassClass.getPackage());
        // 参数列表
        for (int i = 0; i < params.length; i++) {
            map.put(String.valueOf(i),params[i]);
        }
        // 转为JSON字符串

        ObjectMapper mapper = new ObjectMapper();
        String jsonString = "";
        try {
             jsonString = mapper.writeValueAsString(map);
        }catch (JsonProcessingException e){
            e.printStackTrace();
        }
        // 做SHA256 Hash计算，得到一个SHA256摘要作为Key
        return DigestUtils.sha256Hex(jsonString);
    }

    @Override
    public String getAlias() {
        return KeyConfig.REDIS_KEY_GENERATOR;
    }
}
