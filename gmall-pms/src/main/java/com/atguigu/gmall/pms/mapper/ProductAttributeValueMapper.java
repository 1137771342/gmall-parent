package com.atguigu.gmall.pms.mapper;

import com.atguigu.gmall.pms.entity.ProductAttributeValue;
import com.atguigu.gmall.to.es.EsProductAttributeValue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 * 存储产品参数信息的表 Mapper 接口
 * </p>
 *
 * @author Lfy
 * @since 2019-05-08
 */
public interface ProductAttributeValueMapper extends BaseMapper<ProductAttributeValue> {

    /**
     * @param id 保存商品spu的基础数据
     */
    List<EsProductAttributeValue> selectProductBaseAttrAndValue(Long id);
}
