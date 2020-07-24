package com.athuigu.gmall.controller;

import com.athuigu.gmall.service.RedisIncrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Project_Name gmall-parent
 * @Package_Name com.athuigu.gmall.controller
 * @Author yong Huang
 * @date 2020/7/19   15:21
 */
@RestController
public class HelloController {

    @Autowired
    RedisIncrService redisIncrService;

    @GetMapping("/incr")
    public String incr(){
        redisIncrService.incr();
        return "ok";
    }

    /**
     * 测试分布式锁,使用redistemplate lua脚本执行
     * @return
     */
    @GetMapping("/lock")
    public String lockDistributed() throws Exception {
        redisIncrService.lockDistributed();
        return "ok";
    }

    @GetMapping("/jedis/lock")
    public String lockDistributedJedis() throws Exception {
        redisIncrService.lockDistributedJedis();
        return "ok";
    }



    @GetMapping("/redisson/lock")
    public String lockRedisson() throws Exception {
        redisIncrService.lockRedisson();
        return "ok";
    }
}
