package org.spring.springboot.keygenerator;

import java.lang.reflect.Method;

public class KeyConfig {

    // 该值是 keyGenerator 方法的方法名称，如果Bean 指定了名称，则使用指定的名称
    public static final String DEFAULT_KEY_GENERATOR = "keyGenerator";
    public static final String REDIS_KEY_GENERATOR = "redisCacheKeyGenerator";
}
