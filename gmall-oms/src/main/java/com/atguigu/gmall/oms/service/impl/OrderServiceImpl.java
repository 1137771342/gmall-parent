package com.atguigu.gmall.oms.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.dubbo.rpc.RpcContext;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.constant.SysCacheConstant;
import com.atguigu.gmall.oms.entity.Order;
import com.atguigu.gmall.oms.mapper.OrderMapper;
import com.atguigu.gmall.oms.service.OrderService;
import com.atguigu.gmall.ums.service.MemberService;
import com.atguigu.gmall.vo.cart.CartItem;
import com.atguigu.gmall.vo.order.OrderConfirmVo;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * <p>
 * 订单表 服务实现类
 * </p>
 *
 * @author Lfy
 * @since 2019-05-08
 */
@Service
@Component
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Reference
    MemberService memberService;

    @Reference
    CartService cartService;

    @Autowired
    StringRedisTemplate redisTemplate;

    /**
     * 确认订单
     *
     * @param id
     * @return
     */
    @Override
    public OrderConfirmVo confirmOrder(Long id) {
        String accessToken = RpcContext.getContext().getAttachment("accessToken");
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        //收获地址
        confirmVo.setAddresses(memberService.getMemberAddress(id));
        //优惠券信息
        confirmVo.setCoupons(null);
        //商品
        List<CartItem> cartItems = cartService.getCartItemSFromOrder(accessToken);
        confirmVo.setItems(cartItems);
        //返回的orderToken 防止订单重复提交
        String replace = UUID.randomUUID().toString().replace("-", "");
        //手动设置orderToken使用业务逻辑来设置key的过期时间
        String orderToken = replace + "_" + System.currentTimeMillis() + "_" + 10 * 60;
        redisTemplate.opsForSet().add(SysCacheConstant.ORDER_UNIQUE_TOKEN, orderToken);
        confirmVo.setOrderToken(orderToken);
        cartItems.forEach(cartItem -> {
            //设置商品总数量
            confirmVo.setCount(confirmVo.getCount() + cartItem.getCount());
            //设置商品总价格
            confirmVo.setProductTotalPrice(confirmVo.getProductTotalPrice().add(cartItem.getTotalPrice()));
        });
        //设置运费
        confirmVo.setTransPrice(new BigDecimal(10));
        confirmVo.setCouponPrice(null);
        //设置订单总金额，包含运费
        confirmVo.setTotalPrice(confirmVo.getProductTotalPrice().add(confirmVo.getTransPrice()));
        return confirmVo;
    }
}
