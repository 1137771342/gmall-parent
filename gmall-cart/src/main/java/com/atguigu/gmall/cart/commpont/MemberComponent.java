package com.atguigu.gmall.cart.commpont;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.constant.CartCacheConstant;
import com.atguigu.gmall.constant.SysCacheConstant;
import com.atguigu.gmall.ums.entity.Member;
import com.atguigu.gmall.vo.cart.UserCartKey;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

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

    /**
     * @param accessToken
     * @param cartKey
     * @return
     */
    public UserCartKey getCartKey(String accessToken, String cartKey) {
        UserCartKey userCartKey = new UserCartKey();
        Member member = null;
        //获取用户的信息
        if (StringUtils.isNotBlank(accessToken)) {
            member = this.getMemberByAccessToken(accessToken);
        }
        //用户登录
        if (member != null) {
            userCartKey.setLogin(true);
            userCartKey.setUserId(member.getId());
            userCartKey.setFinalCartKey(CartCacheConstant.USER_CART_KEY_PREFIX + member.getId());
            return userCartKey;
            //用户未登录,并且携带了cartKey
        } else if (StringUtils.isNotBlank(cartKey)) {
            userCartKey.setLogin(false);
            userCartKey.setFinalCartKey(CartCacheConstant.TEMP_CART_KEY_PREFIX + cartKey);
            return userCartKey;
            //用户第一次进来给他分配一个购物车
        } else {
            String tempCartKey = UUID.randomUUID().toString().replace("-", "");
            userCartKey.setLogin(false);
            userCartKey.setFinalCartKey(CartCacheConstant.TEMP_CART_KEY_PREFIX + tempCartKey);
            userCartKey.setTempCartKey(tempCartKey);
            return userCartKey;
        }
    }
}

