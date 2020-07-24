package com.athuigu.gmall.config;

import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @Project_Name gmall-parent
 * @Package_Name com.athuigu.gmall.config
 * @Author yong Huang
 * @date 2020/7/19   20:42
 */
@Configuration
public class JedisConfig {

    @Bean
    public JedisPool jedisPool(RedisProperties properties){
        //1、连接工厂中所有信息都有。
        JedisPoolConfig config = new JedisPoolConfig();
        RedisProperties.Pool pool = properties.getJedis().getPool();
        //这些配置
        config.setMaxIdle(pool.getMaxIdle());
        config.setMaxTotal(pool.getMaxActive());
        config.setMaxWaitMillis(100000L);
        JedisPool jedisPool = null;
        jedisPool = new JedisPool(config, properties.getHost(), properties.getPort());
        return jedisPool;
    }
}
