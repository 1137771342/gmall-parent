package com.atguigu.gmall.cart.serivce.impl;


import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.cart.commpont.MemberComponent;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.constant.CartCacheConstant;
import com.atguigu.gmall.ums.entity.Member;
import com.atguigu.gmall.vo.cart.CartItem;
import com.atguigu.gmall.vo.cart.CartResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * @Project_Name gmall-parent
 * @Package_Name com.atguigu.gmall.cart.serivce.impl
 * @Author yong Huang
 * @date 2020/8/15   22:25
 */
@Service
@Component
public class CartServiceImpl implements CartService {

    @Autowired
    private MemberComponent memberComponent;

    /**
     * @param skuId       商品id
     * @param cartKey     uuid 作离线购物车使用
     * @param accessToken 根据这个判断用户是否登录
     * @return 分3种情况
     * 当用户登录的时候,购物车在redis中的key是 cart:user:用户id
     * 未登录的时候 在redis中的key 是        cart:temp:前端传过来的cartKey
     * 当既没有cartKey 和accessToken 代表首次 此时 将要生成一个 cartKey 返给前端,下次用户就可以拿着这个过来了
     */
    @Override
    public CartResponse addToCart(String skuId, String cartKey, String accessToken) {

        //1. 根据accessToken获取用户id的信息
        Member member = memberComponent.getMemberByAccessToken(accessToken);

        String finalCartKey = "";
        //2.用户不为空并且离线购物车有东西 ,进行合并购物车,并且清空 离线购物车
        if (member != null && StringUtils.isNotBlank(cartKey)) {
            margeCart(cartKey, member.getId());
        }

        //3.用户登录了 给user购物车增加一条购物记录
        if (member != null) {
            finalCartKey = CartCacheConstant.USER_CART_KEY_PREFIX + member.getId();
            //3.1 更具skuId 查出商品的信息 ,并给购物车添加数据
            // 3.2 如果购物的一条信息已经存在就给count 增加数量,不存在就新加一条购物数量 ,返回的列表给前端
            CartItem cartItem = addItemToCart(skuId, finalCartKey);
            CartResponse cartResponse = new CartResponse();
            cartResponse.setCartItem(cartItem);
            return cartResponse;
        }

        //4.用户没有登录 给离线购物车增加一条购物记录
        if (StringUtils.isNotBlank(cartKey)) {
            finalCartKey = CartCacheConstant.TEMP_CART_KEY_PREFIX + cartKey;
            //4.1 更具skuId 查出商品的信息 ,并给购物车添加数据
            // 4.2 如果购物的一条信息已经存在就给count 增加数量,不存在就新加一条购物数量 ,返回性的列表给前端
            CartItem cartItem = addItemToCart(skuId, finalCartKey);
            CartResponse cartResponse = new CartResponse();
            cartResponse.setCartItem(cartItem);
            return cartResponse;
        }
        //5.如果以上都没有,就给它生成一个 cartKey,并将购物车的信息放在这个redis里面,且要把生成的这个返回给前端
        String newCartKey = UUID.randomUUID().toString().replace("-", "");
        finalCartKey = CartCacheConstant.TEMP_CART_KEY_PREFIX + newCartKey;
        CartItem cartItem = addItemToCart(skuId, finalCartKey);
        CartResponse cartResponse = new CartResponse();
        cartResponse.setCartItem(cartItem);
        cartResponse.setCartKey(newCartKey);
        return cartResponse;
    }

    /**
     * 添加商品到指定购物车
     *
     * @param skuId
     * @param finalCartKey
     * @return
     */
    private CartItem addItemToCart(String skuId, String finalCartKey) {

        return null;

    }

    /**
     * 合并购物车 可以更具参数去redis中查询
     *
     * @param cartKey 离线购物车
     * @param id      登录后的购物车
     */
    private void margeCart(String cartKey, Long id) {

    }
}
