package com.atguigu.gmall.cart.commpont;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.constant.SysCacheConstant;
import com.atguigu.gmall.ums.entity.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @Project_Name gmall-parent
 * @Package_Name com.atguigu.gmall.cart.commpont
 * @Author yong Huang
 * @date 2020/8/15   23:44
 * Component
 */
@Component
public class MemberComponent {

    @Autowired
    StringRedisTemplate redisTemplate;

    public Member getMemberByAccessToken(String accessToken) {
        String userJSon = redisTemplate.opsForValue().get(SysCacheConstant.LOGIN_MEMBER + accessToken);

        return JSON.parseObject(userJSon, Member.class);
    }
}

