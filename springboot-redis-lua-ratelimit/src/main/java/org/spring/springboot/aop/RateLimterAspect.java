package org.spring.springboot.aop;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spring.springboot.config.CacheConfig;
import org.spring.springboot.keygenerator.KeyConfig;
import org.spring.springboot.keygenerator.KeyGenerator;
import org.spring.springboot.keygenerator.KeyGeneratorSupport;
import org.spring.springboot.keygenerator.RedisCacheKeyGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  限流切面定义
 * </p>
 *
 * @author DevinYang
 * @since 2019-12-10
 */
@Aspect
@Component
public class RateLimterAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimterAspect.class);
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    private DefaultRedisScript<Number> redisluaScript;
    @Autowired
    private RedisCacheKeyGenerator redisCacheKeyGenerator;
    @Autowired
    private KeyGeneratorSupport keyGeneratorSupport;

    // 方式一
    @Pointcut("@annotation(rateLimiter)")
    public void pointCut(RateLimiter rateLimiter) {

    }
//    方式二
//    @Pointcut("@annotation(org.spring.springboot.aop.RateLimiter)")
//    public void pointCut() {
//
//    }

    @Around("@annotation(rateLimiter)")
    public Object around(ProceedingJoinPoint proceedingJoinPoint, RateLimiter rateLimiter) throws Throwable {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("RateLimterHandler[分布式限流处理器]开始执行限流操作");
        }
        Signature signature = proceedingJoinPoint.getSignature();
        if (!(signature instanceof MethodSignature)) {
            throw new IllegalArgumentException("the Annotation @RateLimter must used on method!");
        }
        /**
         * 获取注解参数
         */
        // 限流模块key
        String limitKey = rateLimiter.key();
        Preconditions.checkNotNull(limitKey);

        /**
         * 获取key生成策略
         */
        String keyGeneratorName = rateLimiter.keyGenerator();
        if (StringUtils.isEmpty(keyGeneratorName)){
            keyGeneratorName = KeyConfig.DEFAULT_KEY_GENERATOR;
        }
        MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();
        Method method = methodSignature.getMethod();
        Class<?> targetClass = method.getDeclaringClass();

        KeyGenerator keyGenerator = keyGeneratorSupport.getKeyGenerator(keyGeneratorName);
        String key =limitKey+":"+ keyGenerator.generate(targetClass,method);
        LOGGER.debug("limitKey="+key);

        //key定制：ip:类名:方法名:key
//        MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();
//        Method method = methodSignature.getMethod();
//        Class<?> targetClass = method.getDeclaringClass();
//        RateLimit rateLimit = method.getAnnotation(RateLimit.class);
//        StringBuffer stringBuffer = new StringBuffer();
//        stringBuffer.append(ipAddress).append("-")
//                .append(targetClass.getName()).append("- ")
//                .append(method.getName()).append("-")
//                .append(rateLimit.key());
//        List<String> keys = Collections.singletonList(stringBuffer.toString());

        // 限流阈值
        long limitTimes = rateLimiter.limit();
        // 限流超时时间
        long expireTime = rateLimiter.expire();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("RateLimterHandler[分布式限流处理器]参数值为-limitTimes={},limitTimeout={}", limitTimes, expireTime);
        }
        // 限流提示语
        String message = rateLimiter.message();
        if (StringUtils.isBlank(message)) {
            message = "false";
        }
        /**
         * 执行Lua脚本
         */
        List<String> keyList = new ArrayList();
        // 设置key值为注解中的值
        keyList.add(limitKey);
        /**
         * 调用脚本并执行
         */
        Long result = (Long) redisTemplate.execute(redisluaScript, keyList, expireTime, limitTimes);
        if (result == 0) {
            String msg = "由于超过单位时间=" + expireTime + "-允许的请求次数=" + limitTimes + "[触发限流]";
            LOGGER.debug(msg);
            return message;
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("RateLimterHandler[分布式限流处理器]限流执行结果-result={},请求[正常]响应", result);
        }
        return proceedingJoinPoint.proceed();
    }


}
