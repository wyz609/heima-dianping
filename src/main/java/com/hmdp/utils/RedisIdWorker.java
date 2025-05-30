package com.hmdp.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Class name: Redis
 * Package: com.hmdp.utils
 * Description: 该类用于生成全局唯一ID
 *
 * @Create: 2025/5/20 17:11
 * @Author: jay
 * @Version: 1.0
 */
@Component
public class RedisIdWorker {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    // 开始时间戳
    public static final Long BEGIN_TIMESTAMP = 1640995200L; // 2023-01-01 00:00:00时间戳

    // 序列号长度
    public static final Long COUNT_BITS = 32L; // 序列号的位数

    /**
     * 1位符号位 + 31位时间戳 +32位序列号
     * @param keyPrefix
     * @return
     */
    public long nextId(String keyPrefix){
        // 生成时间戳
        LocalDateTime now = LocalDateTime.now();
        // 1.获取当前时间戳，精确到秒
        long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
        long timestamp = nowSecond - BEGIN_TIMESTAMP;

        // 生成序列号
        //2.1 获取当前日期， 精确到天，每一天一个key，方便统计
        String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        Long increment = stringRedisTemplate.opsForValue().increment("icr:" + keyPrefix + ":" + date);

        // 拼接并返回，简单位运算
        return timestamp << COUNT_BITS | increment;

    }

}
