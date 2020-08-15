package com.atguigu.gmall.ssoserver;

import com.atguigu.gmall.constant.SysCacheConstant;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GmallSsoServerApplicationTests {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Test
    public void contextLoads() {
        String r = UUID.randomUUID().toString().replace("-", "");

        redisTemplate.opsForValue().set(SysCacheConstant.LOGIN_MEMBER + r,
                "老刘全栈工程师", SysCacheConstant.LOGIN_MEMBER_TIMEOUT, TimeUnit.HOURS);
        System.out.println(r);
    }

    @Test
    public void test() {
        String s = redisTemplate.opsForValue().get("测试:redis:83fed81a68b14c598d8c3a8591db40e4");
        System.out.println(s);
    }
}
