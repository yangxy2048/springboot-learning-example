package org.spring.springboot.controller;

import org.databene.contiperf.PerfTest;
import org.databene.contiperf.junit.ContiPerfRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestControllerTest {
    private static final String URL = "http://localhost:8000/ratelimiter";

    @Autowired
    private RestTemplate restTemplate;

    //引入 ContiPerf 进行性能测试
    @Rule
    public ContiPerfRule contiPerfRule = new ContiPerfRule();

    @Test
    @PerfTest(invocations = 10, threads = 2)// //10个线程 执行1次
    public void sendPayment() {
        ResponseEntity<String> response = restTemplate.getForEntity(URL, String.class);
        System.out.println("Thread:" + Thread.currentThread().getName() + "," + response.getBody());

    }

    //使用并发包，利用concurrent包下列进行测试,不过他们没有具体的相应时间:
    @Test
    public void test2() throws Exception {
        ExecutorService executorService = Executors.newCachedThreadPool();
        final Semaphore semaphore = new Semaphore(10);
        final CountDownLatch countDownLatch = new CountDownLatch(100);
        long l = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            final int count = i;
            executorService.execute(() -> {
                try {
                    semaphore.acquire();
                    ResponseEntity<String> response = restTemplate.getForEntity(URL, String.class);
                    System.out.println("Thread:" + Thread.currentThread().getName() + "," + response.getBody());
                    semaphore.release();
                } catch (Exception e) {
                    // log.error("exception" , e);
                }
                countDownLatch.countDown();
            });
        }
        countDownLatch.await();
        long a = System.currentTimeMillis();
        System.out.println(a - l);

        executorService.shutdown();

        //log.info("size:{}" , map.size());
    }

}