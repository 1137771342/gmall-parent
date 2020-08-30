package com.atguigu.gmall.oms.service;

import com.atguigu.gmall.oms.entity.Order;
import com.atguigu.gmall.vo.order.OrderConfirmVo;
import com.atguigu.gmall.vo.order.OrderCreateVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;

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

    /**
     *  创建订单
     * @param frontendPrice 前端传入的价格
     * @param addressId 地址id
     * @param note 订单备注
     * @return
     */
    OrderCreateVo createOrder(BigDecimal frontendPrice, Long addressId, String note);
}
