package com.atguigu.gmall.to.es;

import com.atguigu.gmall.pms.entity.SkuStock;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Project_Name gmall-parent
 * @Package_Name com.atguigu.gmall.to.es
 * @Author yong Huang
 * @date 2020/7/23   22:09
 * sku的基本信息
 */
@Data
public class EsSkuProductInfo extends SkuStock implements Serializable {
    //检索的商品标题
    private String skuTitle;
   //检索的sku对应的不同的值
    private List<EsProductAttributeValue> attributeValues;
}
