package com.hmdp.utils;

import lombok.Data;

import java.time.LocalDateTime;

// 使用万能的Object和过期时间，对原有的代码没有侵入性
@Data
public class RedisData<T> {
    private LocalDateTime expireTime;
    private T data;
}
