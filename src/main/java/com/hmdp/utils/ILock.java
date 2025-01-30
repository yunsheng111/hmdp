package com.hmdp.utils;

/**
 * @ Tool：IntelliJ IDEA
 * @ Author：云生
 * @ Date：2025-01-30-21:22
 * @ Version：1.0
 * @ Description：
 */
public interface ILock {

    /**
     * @description: 尝试获取锁
     * @author: yate
     * @date: 2025/1/30 0030 21:22
     * @param:  timeoutSec 锁持有的超时时间，过期自动释放锁
     * @return:  true代表获取锁成功，false代表获取锁失败
     **/
    boolean tryLock(long timeoutSec);

    /**
     * @description: 释放锁
     * @author: yate
     * @date: 2025/1/30 0030 21:24
     * @param:
     * @return:
     **/
    void unlock();
}
