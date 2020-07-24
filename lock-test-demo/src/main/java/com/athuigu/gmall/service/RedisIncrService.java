package com.athuigu.gmall.service;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @Project_Name gmall-parent
 * @Package_Name com.athuigu.gmall.service
 * @Author yong Huang
 * @date 2020/7/19   15:23
 */
@Service
@Slf4j
public class RedisIncrService {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    JedisPool jedisPool;

    @Autowired
    RedissonClient redisson;


    public synchronized void incr() {
        ValueOperations<String, String> operations = redisTemplate.opsForValue();
        String num = operations.get("num");
        if (num != null) {
            Integer i = Integer.parseInt(num);
            i = i + 1;
            operations.set("num", i.toString());
        }
    }


    public void lockDistributed() throws Exception {
        ValueOperations<String, String> operations = redisTemplate.opsForValue();
        //类似redis的 setnxex
        String token = UUID.randomUUID().toString();
        Boolean lock = operations.setIfAbsent("lock", token, 3L, TimeUnit.SECONDS);
        if (lock) {
            //已经拿到锁了.开始执行业务逻辑
            String num = operations.get("num");
            if (num != null) {
                Integer i = Integer.parseInt(num);
                i = i + 1;
                operations.set("num", i.toString());
            }
            //执行完成后使用lua脚本进行删除锁
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);
            redisTemplate.execute(redisScript, Collections.singletonList("lock"), token);
            log.info("删除锁完成");
        } else {
            Thread.sleep(1000);
            //自旋调用
            lockDistributed();
        }

    }

    public void lockDistributedJedis() throws Exception {
        try {
            Jedis jedis = jedisPool.getResource();
            String token = UUID.randomUUID().toString();
            String lock = jedis.set("lock", token, SetParams.setParams().px(3).nx());
            if (lock != null && "OK".equalsIgnoreCase(lock)) {
                //执行业务
                String num = jedis.get("num");
                Integer i = Integer.parseInt(num);
                i = i + 1;
                jedis.set("num", i.toString());
                //删除锁
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                jedis.eval(script, Collections.singletonList("lock"), Collections.singletonList(token));
                log.info("删除锁完成");
            } else {
                Thread.sleep(1000);
                //自旋调用
                lockDistributedJedis();
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        } finally {
            jedisPool.close();
        }
    }

    public void lockRedisson() {
        RLock lock = redisson.getLock("lock");
        //加锁自带解锁
        try {
            lock.lock(3,TimeUnit.SECONDS);
            log.info("我拿到锁了");
            //执行业务代码
            ValueOperations<String, String> operations = redisTemplate.opsForValue();
            String num = operations.get("num");
            Integer i= Integer.parseInt(num);
            i=i+1;
            operations.set("num",i.toString());
            log.info("我释放锁了");
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

    }
}

