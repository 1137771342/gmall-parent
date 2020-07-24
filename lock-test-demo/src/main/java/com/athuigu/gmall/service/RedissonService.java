package com.athuigu.gmall.service;

import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @Project_Name gmall-parent
 * @Package_Name com.athuigu.gmall.service
 * @Author yong Huang
 * @date 2020/7/20   12:46
 */
@Service
public class RedissonService {

    @Autowired
    RedissonClient redissonClient;

    private String HELLO = "hello";


    public void lock() {
        RLock lock = redissonClient.getLock("lock");
        lock.lock(5, TimeUnit.SECONDS);
    }

    public void unlock() {
        RLock lock = redissonClient.getLock("lock");
        lock.unlock();
    }

    /**
     * 读锁
     *
     * @return
     * @throws InterruptedException
     */
    public String read() throws InterruptedException {
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("helloValue");
        RLock lock = readWriteLock.readLock();
        lock.lock();
        Thread.sleep(2000);
        String s = HELLO;
        lock.unlock();
        return s;
    }

    /**
     * 写锁
     *
     * @return
     */
    public String write() throws InterruptedException {
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("helloValue");
        RLock rLock = readWriteLock.writeLock();
        rLock.lock();
        Thread.sleep(5000);
        HELLO = UUID.randomUUID().toString();
        rLock.unlock();
        return HELLO;
    }

    //停车
    public Boolean tc() throws InterruptedException {
        RSemaphore semaphore = redissonClient.getSemaphore("tcc");
        semaphore.acquire();
        return true;
    }

    //放车
    public Boolean rc() {
        RSemaphore semaphore = redissonClient.getSemaphore("tcc");
        semaphore.release();
        return true;
    }

    public Boolean gogogo() {
        RCountDownLatch countDownLatch = redissonClient.getCountDownLatch("num");
        countDownLatch.countDown();
        System.out.println("我遛了。。。。。");
        return true;

    }


    public Boolean suomen() throws InterruptedException {
        RCountDownLatch countDownLatch = redissonClient.getCountDownLatch("num");
        countDownLatch.trySetCount(5);
        countDownLatch.await();
        System.out.println("我要锁门了");
        return true;
    }
}
