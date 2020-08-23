package com.atguigu.gmall.cart.serivce.impl;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.gmall.cart.commpont.MemberComponent;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.constant.CartCacheConstant;
import com.atguigu.gmall.pms.entity.Product;
import com.atguigu.gmall.pms.entity.SkuStock;
import com.atguigu.gmall.pms.service.ProductService;
import com.atguigu.gmall.pms.service.SkuStockService;
import com.atguigu.gmall.ums.entity.Member;
import com.atguigu.gmall.vo.cart.Cart;
import com.atguigu.gmall.vo.cart.CartItem;
import com.atguigu.gmall.vo.cart.CartResponse;
import com.atguigu.gmall.vo.cart.UserCartKey;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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

    @Reference
    private SkuStockService skuStockService;

    @Autowired
    private RedissonClient redissonClient;

    @Reference
    private ProductService productService;

    /**
     * @param skuId       商品id
     * @param num         购买的商品数量
     * @param cartKey     uuid 作离线购物车使用
     * @param accessToken 根据这个判断用户是否登录
     * @return 分3种情况
     * 当用户登录的时候,购物车在redis中的key是 cart:user:用户id
     * 未登录的时候 在redis中的key 是        cart:temp:前端传过来的cartKey
     * 当既没有cartKey 和accessToken 代表首次 此时 将要生成一个 cartKey 返给前端,下次用户就可以拿着这个过来了
     */
    @Override
    public CartResponse addToCart(Long skuId, Integer num, String cartKey, String accessToken) throws ExecutionException, InterruptedException {

        //1. 根据accessToken获取用户id的信息
        Member member = memberComponent.getMemberByAccessToken(accessToken);

        //2.用户不为空并且离线购物车有东西 ,进行合并购物车,并且清空 离线购物车
        if (member != null && StringUtils.isNotBlank(cartKey)) {
            margeCart(cartKey, member.getId());
        }

        UserCartKey userCartKey = memberComponent.getCartKey(accessToken, cartKey);
        CartItem cartItem = addItemToCart(skuId, num, userCartKey.getFinalCartKey());
        CartResponse cartResponse = new CartResponse();
        cartResponse.setCartItem(cartItem);
        cartResponse.setCartKey(userCartKey.getTempCartKey());
        //返回购物车列表
        cartResponse.setCart(cartList(cartKey, accessToken).getCart());
        return cartResponse;
    }

    @Override
    public CartResponse updateCart(Long skuId, Integer num, String cartKey, String accessToken) {
        CartResponse cartResponse = new CartResponse();
        UserCartKey userCartKey = memberComponent.getCartKey(accessToken, cartKey);
        RMap<String, String> map = redissonClient.getMap(userCartKey.getFinalCartKey());
        if (map != null && !map.isEmpty()) {
            CartItem cartItem = JSON.parseObject(map.get(skuId), CartItem.class);
            cartItem.setCount(num);
            map.put(skuId.toString(), JSON.toJSONString(cartItem));
            cartResponse.setCartItem(cartItem);
        }
        return cartResponse;
    }

    @Override
    public CartResponse cartList(String cartKey, String accessToken) {
        CartResponse cartResponse = new CartResponse();
        UserCartKey userCartKey = memberComponent.getCartKey(accessToken, cartKey);
        //判断购物车是否需要合并
        if (userCartKey.isLogin()) {
            margeCart(cartKey, userCartKey.getUserId());
        }
        RMap<String, String> map = redissonClient.getMap(userCartKey.getFinalCartKey());
        Cart cart = new Cart();
        List<CartItem> cartItems = new ArrayList<>();
        if (map != null && !map.isEmpty()) {
            map.entrySet().forEach(item -> {
                //当key不为checked的时候再往CartItem添加
                if (!item.getKey().equalsIgnoreCase(CartCacheConstant.CART_CHECKED_KEY)) {
                    CartItem cartItem = JSON.parseObject(item.getValue(), CartItem.class);
                    cartItems.add(cartItem);
                }
            });
            cart.setCartItems(cartItems);
        } else {
            //当为空的时候新建一个购物车给前端
            cartResponse.setCartKey(userCartKey.getTempCartKey());
        }
        cartResponse.setCart(cart);
        return cartResponse;
    }

    @Override
    public CartResponse cartDel(Long skuId, String cartKey, String accessToken) {
        UserCartKey userCartKey = memberComponent.getCartKey(accessToken, cartKey);
        String finalCartKey = userCartKey.getFinalCartKey();
        checkItem(Arrays.asList(skuId),false,finalCartKey);
        RMap<String, String> map = redissonClient.getMap(finalCartKey);
        map.remove(skuId.toString());

        CartResponse cartResponse = this.cartList(cartKey, accessToken);
        return cartResponse;


    }

    @Override
    public CartResponse clearCart(String cartKey, String accessToken) {
        UserCartKey userCartKey = memberComponent.getCartKey(accessToken, cartKey);
        RMap<Object, Object> map = redissonClient.getMap(userCartKey.getFinalCartKey());
        map.clear();
        return new CartResponse();
    }

    /**
     * 购物车
     *
     * @param skuIds
     * @param ops
     * @param cartKey
     * @param accessToken
     * @return
     */
    @Override
    public CartResponse checkCartItems(String skuIds, Integer ops, String cartKey, String accessToken) {
        boolean checked = ops == 1 ? true : false;
        List<Long> skuIdList = new ArrayList<>();
        UserCartKey userCartKey = memberComponent.getCartKey(accessToken, cartKey);
        RMap<String, String> map = redissonClient.getMap(userCartKey.getFinalCartKey());
        if (StringUtils.isNotBlank(skuIds)) {
            String[] ids = skuIds.split(",");
            for (String skuId : ids) {
                skuIdList.add(Long.parseLong(skuId));
                if (map != null && !map.isEmpty()) {
                    CartItem cartItem = JSON.parseObject(map.get(skuId), CartItem.class);
                    cartItem.setCheck(checked);
                    //覆盖以前的redis的数据
                    map.put(skuId, JSON.toJSONString(cartItem));
                }
            }
        }
        //修改checked集合的状态
        checkItem(skuIdList, checked, userCartKey.getFinalCartKey());
        CartResponse cartResponse = cartList(cartKey, accessToken);
        return cartResponse;
    }

    /**
     * 维护选择的列表
     *
     * @param skuIdList
     * @param checked
     * @param finalCartKey
     */
    private void checkItem(List<Long> skuIdList, boolean checked, String finalCartKey) {
        RMap<String, String> map = redissonClient.getMap(finalCartKey);
        //redis中选中状态的集合
        String checkedJson = map.get(CartCacheConstant.CART_CHECKED_KEY);
        Set<Long> checkedSet = JSON.parseObject(checkedJson, new TypeReference<Set<Long>>() {
        });
        if (CollectionUtils.isEmpty(checkedSet)) {
            checkedSet = new LinkedHashSet<>();
        }
        if (checked) {
            //选中就往集合中增加数据
            checkedSet.addAll(skuIdList);
        } else {
            //没有选中就移除集合中数据
            checkedSet.removeAll(skuIdList);
        }
        //重新放入缓存中
        map.put(CartCacheConstant.CART_CHECKED_KEY, JSON.toJSONString(checkedSet));

    }

    /**
     * 添加商品到指定购物车
     *
     * @param skuId
     * @param num
     * @param finalCartKey
     * @return
     */
    private CartItem addItemToCart(Long skuId, Integer num, String finalCartKey) throws ExecutionException, InterruptedException {
        CartItem newCartItem = new CartItem();

        CompletableFuture<Void> completableFuture = CompletableFuture.supplyAsync(() -> {
            SkuStock skuStock = skuStockService.getById(skuId);
            return skuStock;
        }).thenAccept(skuStock -> {
            //获取商品的信息
            Product product = productService.getById(skuStock.getProductId());
            BeanUtils.copyProperties(skuStock, newCartItem);
            newCartItem.setSkuId(skuStock.getId());
            newCartItem.setName(product.getName());
            //新购物车购买的商品数量
            newCartItem.setCount(num);
        });
        //根据sku查出商品
        //分布式集合获取redis 中的数据,此时key 就是商品Id value就是每一个的cartItem
        RMap<String, String> map = redissonClient.getMap(finalCartKey);
        String cartItemJson = map.get(skuId.toString());
        //检查购物车中是否已经存在这个购物项
        completableFuture.get();
        if (StringUtils.isNotBlank(cartItemJson)) {
            //如果存在只是数量的增加
            CartItem oldCartItem = JSON.parseObject(cartItemJson, CartItem.class);
            newCartItem.setCount(oldCartItem.getCount() + newCartItem.getCount());
            map.put(skuId.toString(), JSON.toJSONString(newCartItem));
        } else {
            //如果不存在直接加购物车
            map.put(skuId.toString(), JSON.toJSONString(newCartItem));
        }
        //添加购物车时默认为选中zhuangtai
        checkItem(Arrays.asList(skuId),true,finalCartKey);
        return newCartItem;
    }

    /**
     * 合并购物车 可以更具参数去redis中查询
     *
     * @param cartKey 离线购物车
     * @param id      用户id 登录后的购物车
     */
    private void margeCart(String cartKey, Long id) {
        //临时购物车
        String tempCart = CartCacheConstant.TEMP_CART_KEY_PREFIX + cartKey;
        //用户购物车
        String userCart = CartCacheConstant.USER_CART_KEY_PREFIX + id;
        RMap<String, String> map = redissonClient.getMap(tempCart);
        if (map != null && !map.isEmpty()) {
            map.entrySet().forEach(item -> {
                if (!item.getKey().equalsIgnoreCase(CartCacheConstant.CART_CHECKED_KEY)) {
                    String skuId = item.getKey();
                    CartItem cartItem = JSON.parseObject(item.getValue(), CartItem.class);
                    try {
                        addItemToCart(Long.parseLong(skuId), cartItem.getCount(), userCart);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            map.clear();
        }
    }
}
