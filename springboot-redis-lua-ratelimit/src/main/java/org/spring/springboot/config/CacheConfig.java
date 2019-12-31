package org.spring.springboot.config;

import org.springframework.context.annotation.Configuration;

/**
 *
 * @author DevinYang
 * @since 2019-12-10
 */
@Configuration
public class CacheConfig {

    // 定义缓存区，缓存区可以在配置时指定不同的过期时间，作为防止缓存雪崩的一个保护措施
    public static final String COMMON = "COMMON";


//    @Bean
//    KeyGenerator keyGenerator(){
//        return new RedisCacheKeyGenerator();
//    }
}
