package com.atguigu.gmall.rabbitmq.config;


import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Project_Name gmall-parent
 * @Package_Name com.atguigu.gmall.rabbitmq.config
 * @Author yong Huang
 * @date 2020/8/23   12:18
 */
@Configuration
public class RabbitOrderConfig {
    /**
     * 创建交换机
     *
     * @return
     */
    @Bean
    public Exchange orderExchange() {
        return new DirectExchange("order-exchange", true, false, null);
    }

    /**
     * 创建队列
     *
     * @return
     */
    @Bean
    public Queue orderQueue() {
        return new Queue("order_queue", true, false, false);

    }

    /**
     * 创建绑定关系
     * @return
     */
    @Bean
    public Binding orderBinding() {
        return new Binding("order_queue", Binding.DestinationType.QUEUE, "order-exchange",
                "createOrder", null);
    }


}
