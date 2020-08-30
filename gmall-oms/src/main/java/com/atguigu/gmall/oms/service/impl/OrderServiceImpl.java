package com.atguigu.gmall.oms.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.constant.SysCacheConstant;
import com.atguigu.gmall.constant.enums.OrderStatusEnume;
import com.atguigu.gmall.oms.component.MemberComponent;
import com.atguigu.gmall.oms.entity.Order;
import com.atguigu.gmall.oms.entity.OrderItem;
import com.atguigu.gmall.oms.mapper.OrderMapper;
import com.atguigu.gmall.oms.service.OrderItemService;
import com.atguigu.gmall.oms.service.OrderService;
import com.atguigu.gmall.pms.entity.SkuStock;
import com.atguigu.gmall.pms.service.ProductService;
import com.atguigu.gmall.pms.service.SkuStockService;
import com.atguigu.gmall.to.es.EsProduct;
import com.atguigu.gmall.to.es.EsProductAttributeValue;
import com.atguigu.gmall.to.es.EsSkuProductInfo;
import com.atguigu.gmall.ums.entity.Member;
import com.atguigu.gmall.ums.entity.MemberReceiveAddress;
import com.atguigu.gmall.ums.service.MemberService;
import com.atguigu.gmall.vo.cart.CartItem;
import com.atguigu.gmall.vo.order.OrderConfirmVo;
import com.atguigu.gmall.vo.order.OrderCreateVo;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * <p>
 * 订单表 服务实现类
 * </p>
 *
 * @author Lfy
 * @since 2019-05-08
 */
@Service
@Component
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Reference
    MemberService memberService;

    @Reference
    CartService cartService;

    @Reference
    SkuStockService skuStockService;

    @Autowired
    ProductService productService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    MemberComponent memberComponent;

    @Autowired
    OrderMapper orderMapper;

    @Autowired
    OrderItemService orderItemService;


    ThreadLocal<List<CartItem>> threadLocal = new ThreadLocal<>();


    /**
     * 确认订单
     *
     * @param id
     * @return
     */
    @Override
    public OrderConfirmVo confirmOrder(Long id) {
        String accessToken = RpcContext.getContext().getAttachment("accessToken");
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        //收获地址
        confirmVo.setAddresses(memberService.getMemberAddress(id));
        //优惠券信息
        confirmVo.setCoupons(null);
        //商品
        List<CartItem> cartItems = cartService.getCartItemSFromOrder(accessToken);
        confirmVo.setItems(cartItems);
        //返回的orderToken 防止订单重复提交
        String replace = UUID.randomUUID().toString().replace("-", "");
        //手动设置orderToken使用业务逻辑来设置key的过期时间
        String orderToken = replace + "_" + System.currentTimeMillis() + "_" + 10 * 60;
        redisTemplate.opsForSet().add(SysCacheConstant.ORDER_UNIQUE_TOKEN, orderToken);
        confirmVo.setOrderToken(orderToken);
        cartItems.forEach(cartItem -> {
            //设置商品总数量
            confirmVo.setCount(confirmVo.getCount() + cartItem.getCount());
            //设置商品总价格
            confirmVo.setProductTotalPrice(confirmVo.getProductTotalPrice().add(cartItem.getTotalPrice()));
        });
        //设置运费
        confirmVo.setTransPrice(new BigDecimal(10));
        confirmVo.setCouponPrice(null);
        //设置订单总金额，包含运费
        confirmVo.setTotalPrice(confirmVo.getProductTotalPrice().add(confirmVo.getTransPrice()));
        return confirmVo;
    }

    /**
     * @param frontendPrice 前端传入的价格
     * @param addressId     地址id
     * @param note          订单备注
     * @return
     */
    @Override
    public OrderCreateVo createOrder(BigDecimal frontendPrice, Long addressId, String note) {
        String orderToken = RpcContext.getContext().getAttachment("orderToken");
        if (StringUtils.isEmpty(orderToken)) {
            OrderCreateVo orderCreateVo = new OrderCreateVo();
            orderCreateVo.setToken("此次操作出现了错误，请重试");
            return orderCreateVo;
        }
        //校验令牌合法性
        String[] s = orderToken.split("_");
        if (s.length != 3) {
            OrderCreateVo orderCreateVo = new OrderCreateVo();
            orderCreateVo.setToken("非法的操作，请重试");
            return orderCreateVo;
        }
        //检验令牌是否超时
        long creatTime = Long.parseLong(s[0]);
        long timeout = Long.parseLong(s[1]);
        if (System.currentTimeMillis() - creatTime >= timeout) {
            OrderCreateVo orderCreateVo = new OrderCreateVo();
            orderCreateVo.setToken("页面超时，请刷新");
            return orderCreateVo;
        }
        //验证令牌是否正确
        Long remove = redisTemplate.opsForSet().remove(SysCacheConstant.ORDER_UNIQUE_TOKEN, orderToken);
        //代表redis中没有这个值
        if (remove == 0) {
            OrderCreateVo orderCreateVo = new OrderCreateVo();
            orderCreateVo.setToken("创建失败，请重试");
            return orderCreateVo;
        }


        String accessToken = RpcContext.getContext().getAttachment("accessToken");
        Member member = memberComponent.getMemberByAccessToken(accessToken);
        //开始比价
        Boolean flag = validPrice(frontendPrice, accessToken);
        if (!flag) {
            OrderCreateVo createVo = new OrderCreateVo();
            createVo.setLimit(false);
            return createVo;
        }
        //初始化订单orderCreateVo
        OrderCreateVo orderCreateVo = initCreateOrderVo(frontendPrice, addressId, accessToken, member);
        //初始化订单
        Order order = initOrder(frontendPrice, addressId, note, member, orderCreateVo.getOrderSn());
        orderMapper.insert(order);
        //保存订单明细
        saveOrderItem(order);
        return orderCreateVo;
    }

    /**
     * 保存订单明细
     *
     * @param order
     */
    private void saveOrderItem(Order order) {
        //从threadLocal中取出cartItems
        List<CartItem> cartItems = threadLocal.get();
        List<OrderItem> orderItems = new ArrayList<>();
        cartItems.forEach(cartItem -> {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setOrderSn(order.getOrderSn());
            Long skuId = cartItem.getSkuId();
            EsProduct esProduct = productService.productSkuInfo(skuId);
            SkuStock skuStock = new SkuStock();
            List<EsSkuProductInfo> skuProductInfos = esProduct.getSkuProductInfos();
            for (EsSkuProductInfo skuProductInfo : skuProductInfos) {
                if (skuId.equals(skuProductInfo.getId())) {
                    //商品属性信息
                    List<EsProductAttributeValue> attributeValues = skuProductInfo.getAttributeValues();
                    orderItem.setProductAttr(JSON.toJSONString(attributeValues));
                    BeanUtils.copyProperties(skuProductInfo, skuStock);
                }
            }
            orderItem.setProductId(skuStock.getProductId());
            orderItem.setProductPic(skuStock.getPic());
            orderItem.setProductName(esProduct.getName());
            orderItem.setProductBrand(esProduct.getBrandName());
            orderItem.setProductSn(esProduct.getProductSn());
            orderItem.setProductPrice(cartItem.getPrice());
            orderItem.setProductSkuId(skuId);
            orderItem.setProductSkuCode(skuStock.getSkuCode());
            orderItem.setProductCategoryId(esProduct.getProductCategoryId());
            orderItem.setSp1(skuStock.getSp1());
            orderItem.setSp2(skuStock.getSp2());
            orderItem.setSp3(skuStock.getSp3());
            orderItems.add(orderItem);
        });
        orderItemService.saveBatch(orderItems);
    }

    /**
     * 初始化订单vo
     *
     * @param frontendPrice
     * @param addressId
     * @param accessToken
     * @param member
     * @return
     */
    private OrderCreateVo initCreateOrderVo(BigDecimal frontendPrice, Long addressId, String accessToken, Member member) {
        OrderCreateVo orderCreateVo = new OrderCreateVo();
        List<CartItem> cartItems = cartService.getCartItemSFromOrder(accessToken);
        String orderSn = IdWorker.getTimeId();
        orderCreateVo.setOrderSn(orderSn);
        orderCreateVo.setAddressId(addressId);
        orderCreateVo.setMemberId(member.getId());
        orderCreateVo.setCartItems(cartItems);
        orderCreateVo.setTotalPrice(frontendPrice);
        orderCreateVo.setDetailInfo(cartItems.get(0).getName());
        return orderCreateVo;
    }

    /**
     * 初始化订单
     *
     * @param frontendPrice
     * @param addressId
     * @param note
     * @param member
     * @param orderSn
     * @return
     */
    private Order initOrder(BigDecimal frontendPrice, Long addressId, String note, Member member, String orderSn) {
        Order order = new Order();
        order.setMemberId(member.getId());
        order.setOrderSn(orderSn);
        order.setCreateTime(new Date());
        order.setMemberUsername(member.getUsername());
        order.setTotalAmount(frontendPrice);
        order.setFreightAmount(new BigDecimal(10));
        order.setStatus(OrderStatusEnume.UNPAY.getCode());
        order.setAutoConfirmDay(7);
        //保存收获地址
        MemberReceiveAddress address = memberService.getMemberAddressByAddressId(addressId);
        order.setReceiverName(address.getName());
        order.setReceiverPhone(address.getPhoneNumber());
        order.setReceiverPostCode(address.getPostCode());
        order.setReceiverProvince(address.getProvince());
        order.setReceiverCity(address.getCity());
        order.setReceiverRegion(address.getRegion());
        order.setReceiverDetailAddress(address.getDetailAddress());
        order.setNote(note);
        order.setDeleteStatus(0);
        return order;
    }

    /**
     * 校验价格
     *
     * @param frontendPrice
     * @param accessToken
     * @return
     */
    private Boolean validPrice(BigDecimal frontendPrice, String accessToken) {
        List<CartItem> cartItems = cartService.getCartItemSFromOrder(accessToken);
        threadLocal.set(cartItems);
        BigDecimal bigDecimal = new BigDecimal(0);
        for (CartItem cartItem : cartItems) {
            Long skuId = cartItem.getSkuId();
            //根据商品id查询出最新的价格
            SkuStock skuStock = skuStockService.getSkuStockBySkuId(skuId);
            cartItem.setPrice(skuStock.getPrice());
            BigDecimal multiply = skuStock.getPrice().multiply(new BigDecimal(cartItem.getCount()));
            bigDecimal = bigDecimal.add(multiply);
        }
        BigDecimal tranPrice = new BigDecimal(10);
        BigDecimal totalPrice = tranPrice.add(bigDecimal);

        return frontendPrice.compareTo(totalPrice) == 0 ? true : false;
    }
}
