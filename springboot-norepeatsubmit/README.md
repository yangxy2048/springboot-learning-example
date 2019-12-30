##防止重复提交
#####1、注解定义，代码如下：
```
@Target(ElementType.METHOD) 
@Retention(RetentionPolicy.RUNTIME)
public @interface NoRepeatSubmit {
    /**
     * 设置请求锁定时间
     *
     * @return
     */
    int lockTime() default 10;

}
```

#####2、切面定义：
```
@Aspect
@Component
public class RepeatSubmitAspect {


    private static final Logger LOGGER = LoggerFactory.getLogger(RepeatSubmitAspect.class);

    @Autowired
    private RedisLock redisLock;

    @Pointcut("@annotation(noRepeatSubmit)")
    public void pointCut(NoRepeatSubmit noRepeatSubmit) {
    }

    @Around("pointCut(noRepeatSubmit)")
    public Object around(ProceedingJoinPoint pjp, NoRepeatSubmit noRepeatSubmit) throws Throwable {
        int lockSeconds = noRepeatSubmit.lockTime();

        HttpServletRequest request = RequestUtils.getRequest();
        Assert.notNull(request, "request can not null");

        // 此处可以用token或者JSessionId
        String token = request.getHeader("Authorization");
        String path = request.getServletPath();
        String key = getKey(token, path);
        String clientId = getClientId();

        boolean isSuccess = redisLock.tryLock(key, clientId, lockSeconds);
        LOGGER.info("tryLock key = [{}], clientId = [{}]", key, clientId);

        if (isSuccess) {
            LOGGER.info("tryLock success, key = [{}], clientId = [{}]", key, clientId);
            // 获取锁成功
            Object result;

            try {
                // 执行进程
                result = pjp.proceed();
            } finally {
                // 解锁
                redisLock.releaseLock(key, clientId);
                LOGGER.info("releaseLock success, key = [{}], clientId = [{}]", key, clientId);
            }

            return result;

        } else {
            // 获取锁失败，认为是重复提交的请求
            LOGGER.info("tryLock fail, key = [{}]", key);
            return new ApiResult(200, "重复请求，请稍后再试", null);
        }

    }

    private String getKey(String token, String path) {
        return token + path;
    }

    private String getClientId() {
        return UUID.randomUUID().toString();
    }
}
```

#####3、注解使用，@NoRepeatSubmit(lockTime = 30)：
```
    @PostMapping("submit")
    @NoRepeatSubmit(lockTime = 30)
    public Object submit(@RequestBody UserBean userBean) {
        try {
            // 模拟业务场景
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return new ApiResult(200, "成功", userBean.userId);
    }
 ```