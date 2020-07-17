package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.entity.Brand;
import com.atguigu.gmall.vo.PageInfoVo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 品牌表 服务类
 * </p>
 *
 * @author Lfy
 * @since 2019-05-08
 */
public interface BrandService extends IService<Brand> {

    /**
     *
     * @param keyword
     * @param pageNum
     * @param pageSize
     * @return
     */
    PageInfoVo getBrandPageInfo(String keyword, Integer pageNum, Integer pageSize);
}
