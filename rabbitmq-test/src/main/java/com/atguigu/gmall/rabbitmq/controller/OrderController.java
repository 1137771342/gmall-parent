package com.atguigu.gmall.rabbitmq.controller;

import com.atguigu.gmall.rabbitmq.bean.Order;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * @Project_Name gmall-parent
 * @Package_Name com.atguigu.gmall.rabbitmq.controller
 * @Author yong Huang
 * @date 2020/8/23   12:27
 */
@RestController
public class OrderController {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @GetMapping("/create/order")
    public Order createOrder(Long skuId, Integer num, Integer memberId) {
        //创建订单
        Order order = new Order(UUID.randomUUID().toString().replace("-", ""), skuId, num, memberId);
        //发消息
        //rabbitTemplate.convertAndSend("order-exchange", "createOrder", order);
        rabbitTemplate.convertAndSend("user.order.delay.exchange", "order_delay", order);
        return order;
    }
}
