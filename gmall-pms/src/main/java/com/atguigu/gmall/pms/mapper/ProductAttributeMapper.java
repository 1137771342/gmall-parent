package com.atguigu.gmall.pms.mapper;

import com.atguigu.gmall.pms.entity.ProductAttribute;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 * 商品属性参数表 Mapper 接口
 * </p>
 *
 * @author Lfy
 * @since 2019-05-08
 */
public interface ProductAttributeMapper extends BaseMapper<ProductAttribute> {

    /**
     * 根据商品id查询出商品的基本属性的名称
     * @param id
     * @return
     */
    List<ProductAttribute> selectProductSaleAttrName(Long id);
}
