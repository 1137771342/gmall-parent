package com.atguigu.gmall.pms.mapper;

import com.atguigu.gmall.pms.entity.ProductCategory;
import com.atguigu.gmall.vo.product.PmsProductCategoryWithChildrenItem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 * 产品分类 Mapper 接口
 * </p>
 *
 * @author Lfy
 * @since 2019-05-08
 */
public interface ProductCategoryMapper extends BaseMapper<ProductCategory> {
    /**
     * 查询一级分类下的所有分类
     * @return
     * @param id
     */
    List<PmsProductCategoryWithChildrenItem> listCategoryWithChildren(Integer id);
}
