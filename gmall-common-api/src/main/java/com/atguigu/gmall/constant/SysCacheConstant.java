package com.atguigu.gmall.constant;

/**
 * @Project_Name gmall-parent
 * @Package_Name com.atguigu.gmall.constant
 * @Author yong Huang
 * @date 2020/7/18   20:04
 * 专门用于操作缓存的
 */
public class SysCacheConstant {

    public static final String CATEGORY_CACHE_KEY = "category_cache_key";

    public static final String LOGIN_MEMBER = "login:member:";

    public static final Long LOGIN_MEMBER_TIMEOUT = 30L;
    //订单防重复提交设置的前缀
    public static final String ORDER_UNIQUE_TOKEN = "order:unique:token";
}
