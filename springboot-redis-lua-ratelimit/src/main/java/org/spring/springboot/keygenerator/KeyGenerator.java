package org.spring.springboot.keygenerator;

import java.lang.reflect.Method;

public interface KeyGenerator {

    Object generate(Object targetClass, Method method, Object... params);

    String getAlias();
}
