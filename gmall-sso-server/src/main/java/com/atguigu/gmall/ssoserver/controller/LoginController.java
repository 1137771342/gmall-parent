package com.atguigu.gmall.ssoserver.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.constant.SysCacheConstant;
import com.atguigu.gmall.to.CommonResult;
import com.atguigu.gmall.ums.entity.Member;
import com.atguigu.gmall.ums.service.MemberService;
import com.atguigu.gmall.vo.ums.LoginResponseVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.TimeUnit;


/**
 * @Project_Name gmall-parent
 * @Package_Name com.atguigu.gmall.ssoserver.controller
 * @Author yong Huang
 * @date 2020/8/15   9:52
 */
@RestController
public class LoginController {

    @Reference
    MemberService memberService;

    @Autowired
    StringRedisTemplate redisTemplate;


    @GetMapping("/userInfo")
    public CommonResult userInfo(@RequestParam("accessToken") String accessToken) {

        String token = SysCacheConstant.LOGIN_MEMBER + accessToken;
        String redisValue = redisTemplate.opsForValue().get(token);
        Member member = JSON.parseObject(redisValue, Member.class);
        member.setPassword(null);
        member.setId(null);
        return new CommonResult().success(member);
    }


    /**
     * 用户登录
     *
     * @param username
     * @param password
     * @return
     */
    @GetMapping("/login")
    public CommonResult login(@RequestParam("username") String username,
                              @RequestParam("password") String password) {

        Member member = memberService.login(username, password);

        if (member == null) {
            CommonResult result = new CommonResult().failed();
            result.setMessage("账号或密码错误请重新输入");
            return result;
        } else {
            //存在用户 将用户的信息放入redis中并返回一些常用的信息给用户
            String token = UUID.randomUUID().toString().replace("-", "");
            redisTemplate.opsForValue().set(SysCacheConstant.LOGIN_MEMBER + token, JSON.toJSONString(member),
                    SysCacheConstant.LOGIN_MEMBER_TIMEOUT, TimeUnit.DAYS);
            LoginResponseVo vo = new LoginResponseVo();
            BeanUtils.copyProperties(member, vo);
            vo.setAccessToken(token);
            return new CommonResult().success(vo);
        }

    }
}
