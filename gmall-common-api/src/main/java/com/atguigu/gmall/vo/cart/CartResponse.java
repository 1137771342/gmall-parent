package com.atguigu.gmall.vo.cart;

import lombok.Data;

import java.io.Serializable;

/**
 * @Project_Name gmall-parent
 * @Package_Name com.atguigu.gmall.vo.cart
 * @Author yong Huang
 * @date 2020/8/15   23:10
 */
@Data
public class CartResponse implements Serializable {
    /**
     * 购物车
     */
    private Cart cart;
    /**
     * 一件商品
     */
    private CartItem cartItem;

    /**
     * 做离线购物车使用 当用户没有登录的时候,且离线购物车没有东西的时候
     * 后台需生成一个uuid 给用户作为购物车使用,以后用户就拿着这个cartkey 去请求后台
     * 就有购物车了
     */
    private String cartKey;
}
