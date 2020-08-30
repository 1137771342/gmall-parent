package com.atguigu.gmall.oms.component;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.constant.SysCacheConstant;
import com.atguigu.gmall.ums.entity.Member;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @Project_Name gmall-parent
 * @Package_Name com.atguigu.gamll.component
 * @Author yong Huang
 * @date 2020/8/28   0:11
 */
@Component
public class MemberComponent {
    @Autowired
    StringRedisTemplate redisTemplate;

    public Member getMemberByAccessToken(String accessToken) {
        String userJSon = redisTemplate.opsForValue().get(SysCacheConstant.LOGIN_MEMBER + accessToken);
        if (StringUtils.isNoneBlank(userJSon)) {
            return JSON.parseObject(userJSon, Member.class);
        }
        return null;
    }
}
