package com.hmdp.service.impl;

import com.hmdp.service.ILock;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Class name: SimpleRedisLock
 * Package: com.hmdp.service.impl
 * Description:
 *
 * @Create: 2025/5/23 17:27
 * @Author: jay
 * @Version: 1.0
 */
public class SimpleRedisLock implements ILock {
    // 锁的前缀
    private static final String KEY_PREFIX = "lock:";
    // 具体业务名称，将前缀和业务名拼接之后当做key
    private String name;
    // 这里不是@Autowired注入，采用构造器注入，在创建SimpleRedisLock对象时,将RedisTemplate作为参数传入
    private StringRedisTemplate stringRedisTemplate;
    //
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT;

    private static final String ID_PREFIX = UUID.randomUUID() +"-";

    public SimpleRedisLock(String name, StringRedisTemplate stringRedisTemplate){
        this.name = name;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    static{
        UNLOCK_SCRIPT = new DefaultRedisScript<>();
        UNLOCK_SCRIPT.setLocation(new ClassPathResource("unlock.lua"));
        UNLOCK_SCRIPT.setResultType(Long.class);
    }

    @Override
    public boolean tryLock(long timeoutSec) {
        // 获取线程的标识
        String threadId = ID_PREFIX + Thread.currentThread().getId();
        // 获取锁，使用SETNX方法进行加锁，同时设置过期时间，防止死锁
        Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(KEY_PREFIX + name, threadId,
                                                                        timeoutSec, TimeUnit.SECONDS);
        // 自动拆箱可能出现null，这样写可以更稳妥
        return Boolean.TRUE.equals(success);
    }

    @Override
    public void unlock() {
        stringRedisTemplate.execute(UNLOCK_SCRIPT,
                 Collections.singletonList(KEY_PREFIX + name),
                ID_PREFIX + Thread.currentThread().getId());
//        // 获取线程的标识
//        String threadId = ID_PREFIX + Thread.currentThread().getId();
//        // 获取锁中的标识
//        String lockValue = stringRedisTemplate.opsForValue().get(KEY_PREFIX + name);
//        // 判断当前线程是否是锁的持有者
//        if(threadId.equals(lockValue)){
//            // 通过DEL来删除锁
//            stringRedisTemplate.delete(KEY_PREFIX + name);
//        }
    }
}
