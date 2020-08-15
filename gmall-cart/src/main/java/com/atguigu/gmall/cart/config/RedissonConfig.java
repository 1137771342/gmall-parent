package com.atguigu.gmall.cart.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * @Project_Name gmall-parent
 * @Package_Name com.athuigu.gmall.config
 * @Author yong Huang
 * @date 2020/7/20   12:04
 */
@Configuration
public class RedissonConfig {

    /**
     * 设置一个单节点的redisson
     *
     * @return
     * @throws IOException
     */
    @Bean
    RedissonClient redisson() throws IOException {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.1.133:6379");
        return Redisson.create(config);
    }
}
