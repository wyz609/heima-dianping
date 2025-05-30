package com.hmdp.service;

public interface ILock {

    /**
     * 尝试加锁操作，对该方法进行扩展成分布式锁
     * @param timeoutSec 锁的超时时间,过期自动释放
     * @return 是否加锁成功 true表示加锁成功，false表示加锁失败
     */
    boolean tryLock(long timeoutSec);

    /**
     * 释放锁，对锁进行删除操作
     */
    void unlock();

}
