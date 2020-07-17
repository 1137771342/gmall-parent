package com.atguigu.gmall.ums.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.ums.entity.Admin;
import com.atguigu.gmall.ums.mapper.AdminMapper;
import com.atguigu.gmall.ums.service.AdminService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

/**
 * <p>
 * 后台用户表 服务实现类
 * </p>
 *
 * @author Lfy
 * @since 2019-05-08
 */
@Component
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    /**
     * 校验登录
     * @param username
     * @param password
     * @return
     */
    @Override
    public Admin login(String username, String password) {
        //进行md5加密
        String s = DigestUtils.md5DigestAsHex(password.getBytes());
        Admin admin = this.getOne(new QueryWrapper<Admin>().eq("username", username).eq("password", s));
        return admin;
    }

    /**
     * 根据用户名获取信息
     * @param userName
     * @return
     */
    @Override
    public Admin getUserByUserName(String userName) {
        return this.getOne(new QueryWrapper<Admin>().eq("username", userName));
    }
}
