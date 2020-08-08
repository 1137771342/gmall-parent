package com.atguigu.gamll.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.pms.service.ProductService;
import com.atguigu.gmall.to.CommonResult;
import com.atguigu.gmall.to.es.EsProduct;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Project_Name gmall-parent
 * @Package_Name com.atguigu.gamll.controller
 * @Author yong Huang
 * @date 2020/8/8   12:49
 * 商品详情controller
 */
@RestController
public class ProductItemController {

    @Reference
    ProductService productService;


    /**
     * 查询es的单个商品的详情
     *
     * @param id
     * @return
     */
    @GetMapping("/item/{id}.html")
    public CommonResult productInfo(@PathVariable("id") Long id) {
        EsProduct esProduct = productService.productAllInfo(id);
        return new CommonResult().success(esProduct);
    }

    /**
     * 查询es的单个商品的详情
     *
     * @param id 商品的sku信息
     * @return
     */
    @GetMapping("/item/sku/{id}.html")
    public CommonResult productSkuInfo(@PathVariable("id") Long id) {
        EsProduct esProductSku = productService.productSkuInfo(id);
        return new CommonResult().success(esProductSku);
    }





}
