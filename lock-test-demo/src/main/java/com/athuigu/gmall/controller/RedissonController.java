package com.athuigu.gmall.controller;

import com.athuigu.gmall.service.RedissonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Project_Name gmall-parent
 * @Package_Name com.athuigu.gmall.controller
 * @Author yong Huang
 * @date 2020/7/20   12:11
 */
@RestController
public class RedissonController {

    @Autowired
    RedissonService redissonService;

    /**
     * 加锁
     *
     * @return
     */
    @GetMapping("/test/lock")
    public String lock() {
        redissonService.lock();
        return "ok";
    }

    @GetMapping("/test/unlock")
    public String unlock() {
        redissonService.unlock();
        return "ok";
    }

    //读写锁
    @GetMapping("/read")
    public String read() throws InterruptedException {
        return redissonService.read();
    }

    //读写锁
    @GetMapping("/write")
    public String write() throws InterruptedException {
        return redissonService.write();
    }

    //信号量
    @GetMapping("/tc")
    public Boolean tc() throws InterruptedException {
        return redissonService.tc();
    }

    @GetMapping("/rc")
    public Boolean rc() {
        return redissonService.rc();
    }

    /**
     * 走人
     *
     * @return
     */
    @GetMapping("/gogogo")
    public Boolean gogogo() {
        return redissonService.gogogo();
    }

    @GetMapping("/suomen")
    public String suomen() throws InterruptedException {
        Boolean flag = redissonService.suomen();
        return flag ? "锁门了" : "门没锁还有人";

    }


}
