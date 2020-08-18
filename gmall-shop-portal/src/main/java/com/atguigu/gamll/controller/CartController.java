package com.atguigu.gamll.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.to.CommonResult;
import com.atguigu.gmall.vo.cart.CartResponse;
import org.springframework.web.bind.annotation.*;

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


    /**
     * 更新购物车
     *
     * @param skuId
     * @param num
     * @param cartKey
     * @param accessToken
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @PostMapping("/update")
    public CommonResult updateCart(@RequestParam("skuId") Long skuId,
                                   @RequestParam(value = "num", defaultValue = "1") Integer num,
                                   @RequestParam(value = "cartKey", required = false) String cartKey,
                                   @RequestParam(value = "accessToken", required = false) String accessToken) throws ExecutionException, InterruptedException {

        CartResponse response = cartService.updateCart(skuId, num, cartKey, accessToken);
        return new CommonResult().success(response);

    }

    /**
     * 查询购物车列表
     *
     * @param cartKey
     * @param accessToken
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @GetMapping("/list")
    public CommonResult cartList(@RequestParam(value = "cartKey", required = false) String cartKey,
                                 @RequestParam(value = "accessToken", required = false) String accessToken) throws ExecutionException, InterruptedException {

        CartResponse response = cartService.cartList(cartKey, accessToken);
        return new CommonResult().success(response);

    }

    /**
     * 删除购物车
     * @param skuId
     * @param cartKey
     * @param accessToken
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @GetMapping("/cartDel")
    public CommonResult cartDel(@RequestParam("skuId") Long skuId,
                                 @RequestParam(value = "cartKey", required = false) String cartKey,
                                 @RequestParam(value = "accessToken", required = false) String accessToken) throws ExecutionException, InterruptedException {

        CartResponse response = cartService.cartDel(skuId,cartKey, accessToken);
        return new CommonResult().success(response);
    }


    /**
     * 清理购物车
     * @param cartKey
     * @param accessToken
     * @return
     */

    @GetMapping("/clear")
    public CommonResult cartClear(
            @RequestParam(value = "cartKey",required = false) String cartKey,
            @RequestParam(value = "accessToken",required = false) String accessToken){

        CartResponse cartResponse = cartService.clearCart(cartKey,accessToken);
        return new CommonResult().success(cartResponse);
    }

    /**
     * 购物车选中或不选中
     * @param skuIds
     * @param ops
     * @param cartKey
     * @param accessToken
     * @return
     */
    @PostMapping("/check")
    public CommonResult cartCheck(@RequestParam("skuIds") String skuIds,
                                  @RequestParam(value = "ops",defaultValue = "1") Integer ops,
                                  @RequestParam(value = "cartKey",required = false) String cartKey,
                                  @RequestParam(value = "accessToken",required = false) String accessToken){


        CartResponse cartResponse = cartService.checkCartItems(skuIds,ops,cartKey,accessToken);
        return new CommonResult().success(cartResponse);
    }



}
