package com.atguigu.gamll.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.constant.SysCacheConstant;
import com.atguigu.gmall.oms.service.OrderService;
import com.atguigu.gmall.to.CommonResult;
import com.atguigu.gmall.ums.entity.Member;
import com.atguigu.gmall.vo.order.OrderConfirmVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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
        RpcContext.getContext().setAttachment("accessToken",accessToken);
        OrderConfirmVo confirmVo=orderService.confirmOrder(member.getId());
        return new CommonResult().success(confirmVo);
    }
}
