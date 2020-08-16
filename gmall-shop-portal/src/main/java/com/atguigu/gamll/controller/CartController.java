package com.atguigu.gamll.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.to.CommonResult;
import com.atguigu.gmall.vo.cart.CartResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

/**
 * @Project_Name gmall-parent
 * @Package_Name com.atguigu.gamll.controller
 * @Author yong Huang
 * @date 2020/8/15   19:20
 */
@RestController
@RequestMapping("/cart")
public class CartController {


    @Reference
    CartService cartService;

    /**
     * 添加商品到购物车
     *
     * @param skuId       商品id
     * @param cartKey     uuid 作离线购物车使用
     * @param accessToken 根据这个判断用户是否登录
     * @return
     */
    @PostMapping("/add")
    public CommonResult addToCart(@RequestParam("skuId") Long skuId,
                                  @RequestParam(value = "num", defaultValue = "1") Integer num,
                                  @RequestParam(value = "cartKey", required = false) String cartKey,
                                  @RequestParam(value = "accessToken", required = false) String accessToken) throws ExecutionException, InterruptedException {

        CartResponse response = cartService.addToCart(skuId, num, cartKey, accessToken);
        return new CommonResult().success(response);

    }


}
