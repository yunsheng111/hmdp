package com.hmdp.utils;

import cn.hutool.core.lang.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.concurrent.TimeUnit;



/**
 * @ Tool：IntelliJ IDEA
 * @ Author：云生
 * @ Date：2025-01-30-21:24
 * @ Version：1.0
 * @ Description：
 */
@Slf4j
public class SimleRedisLock implements ILock {

    private String name;
    private StringRedisTemplate stringRedisTemplate;

    // 提前加载并缓存 Lua 脚本
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT;
    static {
        UNLOCK_SCRIPT = new DefaultRedisScript<>();
        UNLOCK_SCRIPT.setLocation(new ClassPathResource("unlock.lua"));
        UNLOCK_SCRIPT.setResultType(Long.class);
    }


    public SimleRedisLock(String name, StringRedisTemplate stringRedisTemplate) {
        this.name = name;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    //锁的前缀
    private static final String KEY_PREFIX = "lock:";
    //线程id的前缀,区分不同线程
    private static final String THREAD_ID_PREFIX = UUID.randomUUID().toString(true)+"-";

    @Override
    public boolean tryLock(long timeoutSec) {
        //获取线程标识
        String threadId = THREAD_ID_PREFIX + Thread.currentThread().getId();
        log.info("尝试获取锁，线程ID：{}，锁名称：{}", threadId, name);
        //获取锁
        Boolean success = stringRedisTemplate.opsForValue()
                .setIfAbsent(KEY_PREFIX + name, threadId + "", timeoutSec, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(success);
    }

    @Override
    public void unlock() {
        // 调用lua脚本
        stringRedisTemplate.execute(
                UNLOCK_SCRIPT,
                Collections.singletonList(KEY_PREFIX + name),
                THREAD_ID_PREFIX + Thread.currentThread().getId());
    }

   /* public void unlock() {
        //获取当前线程标识
        String threadId = THREAD_ID_PREFIX + Thread.currentThread().getId();
        log.info("释放锁，线程ID：{}，锁名称：{}", threadId, name);
        //获取锁中的线程标识
        String id = stringRedisTemplate.opsForValue().get(KEY_PREFIX + name);
        //判断是否是当前线程加的锁
        if (threadId.equals(id)) {
            //释放锁
            stringRedisTemplate.delete(KEY_PREFIX + name);
        }
    }*/
}
