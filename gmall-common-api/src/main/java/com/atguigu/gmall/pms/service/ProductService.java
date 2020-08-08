package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.entity.Product;
import com.atguigu.gmall.to.es.EsProduct;
import com.atguigu.gmall.vo.PageInfoVo;
import com.atguigu.gmall.vo.product.PmsProductParam;
import com.atguigu.gmall.vo.product.PmsProductQueryParam;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 商品信息 服务类
 * </p>
 *
 * @author Lfy
 * @since 2019-05-08
 */
public interface ProductService extends IService<Product> {

    PageInfoVo pageInfo(PmsProductQueryParam productQueryParam);

    /**
     * 根据商品id获取商品
     * @param id
     * @return
     */
    Product getProductInfo(Long id);
    /**
     * 保存商品
     * @param productParam
     */
    void saveProduct(PmsProductParam productParam);

    /**
     *商品批量上下架
     * @param ids
     * @param publishStatus
     */
    void batchUpdatePublishStatus (List<Long> ids, Integer publishStatus);

    /**
     * 获取商品详情
     *
     * @param id
     * @return
     */
    EsProduct productAllInfo(Long id);

    /**
     * 查询商品的sku信息
     * @param id
     * @return
     */
    EsProduct productSkuInfo(Long id);
}
