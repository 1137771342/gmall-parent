package com.atguigu.gmall.vo.cart;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @Project_Name gmall-parent
 * @Package_Name com.atguigu.gmall.cart
 * @Author yong Huang
 * @date 2020/8/15   19:21
 * 购物车
 */
@Setter
public class Cart implements Serializable {

    @Getter
    private List<CartItem> cartItems;

    /**
     * 商品总数
     */
    private Integer count;
    /**
     * 已选中商品的总价
     */
    private BigDecimal totalPrice;

    //获取总数
    public Integer getCount() {

        AtomicInteger integer = new AtomicInteger(0);
        if (CollectionUtils.isNotEmpty(cartItems)) {
            cartItems.forEach(cartItem -> {
                integer.getAndAdd(cartItem.getCount());
            });
            return integer.get();
        } else {
            return 0;
        }
    }

    public BigDecimal getTotalPrice() {
        AtomicReference<BigDecimal> allTotal = new AtomicReference<>(new BigDecimal("0"));
        if (CollectionUtils.isNotEmpty(cartItems)) {
            cartItems.forEach(cartItem -> {
                BigDecimal add = allTotal.get().add(cartItem.getTotalPrice());
                allTotal.set(add);
            });
            return allTotal.get();
        } else {
            return new BigDecimal(0);
        }
    }
}
