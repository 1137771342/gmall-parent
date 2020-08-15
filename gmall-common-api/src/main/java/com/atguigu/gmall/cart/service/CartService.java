package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.vo.cart.CartResponse;

/**
 * @Project_Name gmall-parent
 * @Package_Name com.atguigu.gmall.cart.service
 * @Author yong Huang
 * @date 2020/8/15   19:51
 */
public interface CartService {

    /**
     * 添加商品到购物车
     *
     * @param skuId       商品id
     * @param cartKey     uuid 作离线购物车使用
     * @param accessToken 根据这个判断用户是否登录
     * @return
     */
    CartResponse addToCart(String skuId, String cartKey, String accessToken);
}
