package com.atguigu.gmall.oms.service;

import com.atguigu.gmall.oms.entity.Order;
import com.atguigu.gmall.vo.order.OrderConfirmVo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 订单表 服务类
 * </p>
 *
 * @author Lfy
 * @since 2019-05-08
 */
public interface OrderService extends IService<Order> {

    /**
     * 确认订单
     * @param id
     * @return
     */
    OrderConfirmVo confirmOrder(Long id);

}
