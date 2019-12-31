package org.spring.springboot.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.*;
import org.springframework.scripting.support.ResourceScriptSource;

import java.time.Duration;

/**
 * <p>
 * Redis默认使用Jdk序列化机制（将对象序列化到Redis中必须要实现Serializable接口且缓存的数据是二进制），若要将数据以Json的形式序列化，可采用以下两种方式
 *
 * 　　1.使用第三方jar包将对象转化为json
 *
 * 　　2.更改redisTemplate默认的序列化规则
 * </p>
 *
 * @author DevinYang
 * @since 2019-12-20
 */
@Configuration
@EnableCaching
public class RedisCacheConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisCacheConfig.class);

//    @Bean
//    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory){
//        RedisTemplate<String, Object> template = new RedisTemplate<>();
//        template.setConnectionFactory(redisConnectionFactory);
//        template.setDefaultSerializer(new Jackson2JsonRedisSerializer<>(Object.class));
//        LOGGER.info("Springboot RedisTemplate 加载完成");
//        return template;
//    }

    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // 使用Jackson2JsonRedisSerialize 替换默认序列化
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);

        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);

        // 设置value的序列化规则和 key的序列化规则
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.afterPropertiesSet();
        LOGGER.info("Springboot RedisTemplate 加载完成");
        return redisTemplate;
    }


//    @Primary
//    @Bean
//    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
//        //初始化一个RedisCacheWriter
//        RedisCacheWriter redisCacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory);
//        //设置CacheManager的值序列化方式为json序列化
//        RedisSerializer<Object> jsonSerializer = new GenericJackson2JsonRedisSerializer();
//        RedisSerializationContext.SerializationPair<Object> pair = RedisSerializationContext.SerializationPair
//                .fromSerializer(jsonSerializer);
//        RedisCacheConfiguration defaultCacheConfig=RedisCacheConfiguration.defaultCacheConfig()
//                .serializeValuesWith(pair);
//        //设置默认超过期时间是30秒
//        defaultCacheConfig.entryTtl(Duration.ofSeconds(30));
//        //初始化RedisCacheManager
//        if (LOGGER.isDebugEnabled()) {
//            LOGGER.debug("Springboot Redis cacheManager 加载完成");
//        }
//
//        return new RedisCacheManager(redisCacheWriter, defaultCacheConfig);
//    }

    /**
     * 读取lua配置脚本
     * @return
     */
    @Bean
    public DefaultRedisScript<Number> redisluaScript() {
        DefaultRedisScript<Number> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("rateLimit.lua")));
        redisScript.setResultType(Number.class);
        return redisScript;
    }


}
