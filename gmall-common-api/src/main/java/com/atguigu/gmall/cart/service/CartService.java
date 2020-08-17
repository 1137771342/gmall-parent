package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.vo.cart.CartResponse;

import java.util.concurrent.ExecutionException;

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
     * @param num         商品的数量
     * @param cartKey     uuid 作离线购物车使用
     * @param accessToken 根据这个判断用户是否登录
     * @return
     */
    CartResponse addToCart(Long skuId, Integer num, String cartKey, String accessToken) throws ExecutionException, InterruptedException;

    /**
     *
     * @param skuId
     * @param num
     * @param cartKey
     * @param accessToken
     * @return
     */
    CartResponse updateCart(Long skuId, Integer num, String cartKey, String accessToken);

    /**
     * 查询购物车列表
     * @param cartKey
     * @param accessToken
     * @return
     */
    CartResponse cartList(String cartKey, String accessToken);


    /**
     * 删除购物车
     * @param skuId
     * @param cartKey
     * @param accessToken
     * @return
     */
    CartResponse cartDel(Long skuId, String cartKey, String accessToken);

    /**
     * 清空购物车
     * @param cartKey
     * @param accessToken
     * @return
     */
    CartResponse clearCart(String cartKey, String accessToken);

}
