package com.atguigu.gmall.ums.service;

import com.atguigu.gmall.ums.entity.Member;
import com.atguigu.gmall.ums.entity.MemberReceiveAddress;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 会员表 服务类
 * </p>
 *
 * @author Lfy
 * @since 2019-05-08
 */
public interface MemberService extends IService<Member> {

    /**
     * 用户登录的方法
     * @param username
     * @param password
     * @return
     */
    Member login(String username, String password);

    /**
     * 获取地址
     * @param id
     * @return
     */
    List<MemberReceiveAddress> getMemberAddress(Long id);
}
