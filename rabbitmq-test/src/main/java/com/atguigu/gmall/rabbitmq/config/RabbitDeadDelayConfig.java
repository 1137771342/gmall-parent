package com.atguigu.gmall.rabbitmq.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @Project_Name gmall-parent
 * @Package_Name com.atguigu.gmall.rabbitmq.config
 * @Author yong Huang
 * @date 2020/8/23   20:44
 */
@Configuration
public class RabbitDeadDelayConfig {

    /**
     * 普通交换机
     * @return
     */
    @Bean
    public Exchange delayExchange() {
        return new DirectExchange("user.order.delay.exchange", true, false);
    }

    @Bean
    public Binding delayBinding() {
        return new Binding("user.order.delay.queue",
                Binding.DestinationType.QUEUE, "user.order.delay.exchange",
                "order_delay", null);
    }

    @Bean
    public Queue delayQueue() {
        Map<String, Object> arguments =new HashMap<>();
        arguments.put("x-message-ttl", 10000);
        //消息死了交给那个交换机
        arguments.put("x-dead-letter-exchange", "user.order.exchange");
        //消息死了交给那个路由键
        arguments.put("x-dead-letter-routing-key", "order");
        return new Queue("user.order.delay.queue", true, false, false, arguments);
    }



    /**
     * 死信队列
     * @return
     */



    @Bean
    public Exchange deadExchange() {
        return new DirectExchange("user.order.exchange", true, false);
    }

    @Bean
    public Binding deadBinding() {
        return new Binding("user.order.queue",
                Binding.DestinationType.QUEUE, "user.order.exchange",
                "order", null);
    }

    @Bean
    public Queue deadQueue() {
        return new Queue("user.order.queue", true, false, false, null);
    }



}
