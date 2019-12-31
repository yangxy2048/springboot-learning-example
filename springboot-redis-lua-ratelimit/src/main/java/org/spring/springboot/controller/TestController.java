package org.spring.springboot.controller;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spring.springboot.aop.RateLimiter;
import org.spring.springboot.config.CacheConfig;
import org.spring.springboot.keygenerator.KeyConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicInteger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * @author DevinYang
 * @since 2019-12-20
 */
@RestController
public class TestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestController.class);
    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    private static final String MESSAGE = "{\"code\":\"400\",\"msg\":\"FAIL\",\"desc\":\"触发限流\"}";

    @RequestMapping("ratelimiter")
    @RateLimiter(key = "xppay:1.0.0",keyGenerator= KeyConfig.REDIS_KEY_GENERATOR, limit = 5, expire = 10,message = MESSAGE)
    public String sendPayment(HttpServletRequest request) throws Exception {
        RedisAtomicInteger entityIdCounter = new RedisAtomicInteger("entityIdCounter", redisTemplate.getConnectionFactory());
        String date = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss.SSS");
        return date + " 正常请求次数：" + entityIdCounter.getAndIncrement();
    }
}
