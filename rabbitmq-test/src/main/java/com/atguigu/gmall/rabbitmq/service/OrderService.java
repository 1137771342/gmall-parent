package com.atguigu.gmall.rabbitmq.service;

import com.atguigu.gmall.rabbitmq.bean.Order;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @Project_Name gmall-parent
 * @Package_Name com.atguigu.gmall.rabbitmq.service
 * @Author yong Huang
 * @date 2020/8/23   16:44
 */
@Component
public class OrderService {

    @RabbitListener(queues = {"order_queue"})
    public void receiveOrder(Order order) {
        System.out.println("监听到订单已经生成");
        Long skuId = order.getSkuId();
        Integer num = order.getNum();
        System.out.println("库存系统正在扣除【" + skuId + "】商品的数量，此次扣除【" + num + "】件");

        if (num % 2 == 0) {
            System.out.println("库存系统扣除【" + skuId + "】库存失败");
            throw new RuntimeException("库存扣减失败");
        }else {
            System.out.println("扣减成功");
        }
    }

    /**
     * 监听关单的
     * @param order
     * @param message
     * @param channel
     */
    @RabbitListener(queues = {"user.order.queue"})
    public void closeOrder(Order order , Message message, Channel channel) throws IOException {
        System.out.println("收到过期订单："+order+"正在关闭订单");
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }
}


