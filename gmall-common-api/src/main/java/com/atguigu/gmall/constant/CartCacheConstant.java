package com.atguigu.gmall.constant;

/**
 * @Project_Name gmall-parent
 * @Package_Name com.atguigu.gmall.constant
 * @Author yong Huang
 * @date 2020/8/15   23:55
 */
public class CartCacheConstant {

    public final static String TEMP_CART_KEY_PREFIX = "cart:temp:";//后面加cartKey
    public final static String USER_CART_KEY_PREFIX = "cart:user:";//后面加用户id
    public final static String CART_CHECKED_KEY = "checked";//购物车在redis中存储哪些被选中用的key

}
