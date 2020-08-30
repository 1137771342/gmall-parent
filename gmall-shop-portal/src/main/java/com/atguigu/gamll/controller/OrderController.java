package com.atguigu.gamll.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.constant.SysCacheConstant;
import com.atguigu.gmall.oms.service.OrderService;
import com.atguigu.gmall.to.CommonResult;
import com.atguigu.gmall.ums.entity.Member;
import com.atguigu.gmall.vo.order.OrderConfirmVo;
import com.atguigu.gmall.vo.order.OrderCreateVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * @Project_Name gmall-parent
 * @Package_Name com.atguigu.gamll.controller
 * @Author yong Huang
 * @date 2020/8/25   21:30
 */
@RestController
public class OrderController {

    @Reference
    OrderService orderService;

    @Autowired
    StringRedisTemplate redisTemplate;


    /**
     * @param accessToken 用户登录的token
     * @return
     */
    @GetMapping("/confirmOrder")
    public CommonResult confirmOrder(String accessToken) {
        //去redis中检查用户是否存在
        String memberJson = redisTemplate.opsForValue().get(SysCacheConstant.LOGIN_MEMBER + accessToken);
        if (StringUtils.isEmpty(accessToken) || StringUtils.isEmpty(memberJson)) {
            CommonResult commonResult = new CommonResult().failed();
            commonResult.setMessage("未登录，请先登录");
            return commonResult;
        }
        Member member = JSON.parseObject(memberJson, Member.class);

        //利用rpc隐式传参 将accessToken传过去
        RpcContext.getContext().setAttachment("accessToken", accessToken);
        OrderConfirmVo confirmVo = orderService.confirmOrder(member.getId());
        return new CommonResult().success(confirmVo);
    }

    /**
     * 创建订单
     *
     * @param totalPrice  前端传入的总价格
     * @param accessToken 登录的
     * @param addressId   地址id
     * @param note        订单备注
     * @param orderToken  防重上一步的orderToken
     * @return
     */
    @PostMapping("/createOrder")
    public CommonResult createOrder(@RequestParam("totalPrice") BigDecimal totalPrice,
                                    @RequestParam("accessToken") String accessToken,
                                    @RequestParam("addressId") Long addressId,
                                    @RequestParam(value = "note", required = false) String note,
                                    @RequestParam("orderToken") String orderToken) {
        RpcContext.getContext().setAttachment("accessToken", accessToken);
        RpcContext.getContext().setAttachment("orderToken", orderToken);
        OrderCreateVo orderCreateVo = orderService.createOrder(totalPrice, addressId, note);
        if (StringUtils.isNoneBlank(orderCreateVo.getToken())) {
            CommonResult commonResult = new CommonResult();
            commonResult.setMessage(orderCreateVo.getToken());
            return commonResult.failed();
        }
        return new CommonResult().success(orderCreateVo);

    }
}
