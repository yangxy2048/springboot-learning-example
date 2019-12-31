package org.spring.springboot.keygenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author DevinYang
 * @since 2019-12-14
 */
@Component
public class KeyGeneratorSupport implements InitializingBean, ApplicationContextAware {
    private static final Logger log = LoggerFactory.getLogger(KeyGeneratorSupport.class);

    private static Map<String, KeyGenerator> containers =  new ConcurrentHashMap<>(10);
    private  ApplicationContext applicationContext;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, KeyGenerator> strategys = this.applicationContext.getBeansOfType(KeyGenerator.class);
        containers.putAll(strategys);

        for (Map.Entry<String, KeyGenerator> entry : strategys.entrySet()) {
            KeyGenerator cs = entry.getValue();

            containers.put(cs.getAlias(), cs);
        }
        log.info("[afterPropertiesSet] containers' size: {}", containers.size());
    }

    public boolean isEmpty(){
        return containers.isEmpty();
    }

    public KeyGenerator getKeyGenerator(String serviceName){
        if (this.isEmpty()){
            throw new RuntimeException("containers is empty...");
        }
        KeyGenerator keyGenerator = containers.get(serviceName);
        if (keyGenerator==null){
            throw new RuntimeException("containers is empty...");
        }
        return keyGenerator;
    }

}
