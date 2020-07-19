package com.atguigu.gmall.pms.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.pms.entity.Product;
import com.atguigu.gmall.pms.entity.ProductAttributeValue;
import com.atguigu.gmall.pms.entity.ProductFullReduction;
import com.atguigu.gmall.pms.entity.SkuStock;
import com.atguigu.gmall.pms.mapper.*;
import com.atguigu.gmall.pms.service.ProductService;
import com.atguigu.gmall.vo.PageInfoVo;
import com.atguigu.gmall.vo.product.PmsProductParam;
import com.atguigu.gmall.vo.product.PmsProductQueryParam;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * <p>
 * 商品信息 服务实现类
 * </p>
 *
 * @author Lfy
 * @since 2019-05-08
 */
@Service
@Component
@Slf4j
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Autowired
    ProductMapper productMapper;
    @Autowired
    ProductAttributeValueMapper productAttributeValueMapper;
    //满减
    @Autowired
    ProductFullReductionMapper productFullReductionMapper;
    //阶梯价格表
    @Autowired
    ProductLadderMapper productLadderMapper;
    //库存
    @Autowired
    SkuStockMapper skuStockMapper;

    ThreadLocal<Long> threadLocal = new ThreadLocal<>();


    @Override
    public PageInfoVo pageInfo(PmsProductQueryParam param) {
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        if (param.getBrandId() != null) {
            //前端传了
            wrapper.eq("brand_id", param.getBrandId());
        }
        if (!StringUtils.isEmpty(param.getKeyword())) {
            wrapper.like("name", param.getKeyword());
        }
        if (param.getProductCategoryId() != null) {
            wrapper.eq("product_category_id", param.getProductCategoryId());
        }
        if (!StringUtils.isEmpty(param.getProductSn())) {
            wrapper.like("product_sn", param.getProductSn());
        }
        if (param.getPublishStatus() != null) {
            wrapper.eq("publish_status", param.getPublishStatus());
        }
        if (param.getVerifyStatus() != null) {
            wrapper.eq("verify_status", param.getVerifyStatus());
        }
        IPage<Product> page = this.page(new Page<Product>(param.getPageNum(), param.getPageSize()), wrapper);
        return new PageInfoVo(page.getTotal(), page.getPages(), page.getSize(), page.getRecords(), page.getCurrent());
    }


    /**
     * 保存商品
     *
     * @param productParam
     */
    @Override
    @Transactional
    public void saveProduct(PmsProductParam productParam) {
        //获取代理对象
        ProductServiceImpl proxy = (ProductServiceImpl) AopContext.currentProxy();
        //保存商品的基本信息
        proxy.saveBaseProduct(productParam);
        //保存商品的属性信息
        proxy.saveProductAttributeValue(productParam);
        //保存满减信息
        proxy.saveFullReduction(productParam);
        //保存阶梯价格
        proxy.saveProductLadder(productParam);
        //保存商品库存表
        proxy.saveSkuStock(productParam);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveBaseProduct(PmsProductParam productParam) {
        Product product = new Product();
        BeanUtils.copyProperties(productParam, product);
        productMapper.insert(product);
        log.info("刚才的商品id是{}", product.getId());
        threadLocal.set(product.getId());
        log.info("当前的线程号...{}当前线程...{}", Thread.currentThread().getId(), Thread.currentThread().getName());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveSkuStock(PmsProductParam productParam) {
        for (int i = 1; i < productParam.getSkuStockList().size(); i++) {
            SkuStock skuStock = productParam.getSkuStockList().get(i - 1);
            if (StringUtils.isEmpty(skuStock.getSkuCode())) {
                skuStock.setSkuCode(threadLocal.get() + "_" + i);
            }
            skuStock.setProductId(threadLocal.get());
            skuStockMapper.insert(skuStock);
        }
        log.info("当前的线程号...{}当前线程...{}", Thread.currentThread().getId(), Thread.currentThread().getName());

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveProductLadder(PmsProductParam productParam) {
        productParam.getProductLadderList().forEach(o -> {
            o.setProductId(threadLocal.get());
            productLadderMapper.insert(o);
        });
        int i= 10/0;
        log.info("当前的线程号...{}当前线程...{}", Thread.currentThread().getId(), Thread.currentThread().getName());

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveFullReduction(PmsProductParam productParam) {
        List<ProductFullReduction> fullReductionList = productParam.getProductFullReductionList();
        fullReductionList.forEach(fullReduction -> {
            fullReduction.setProductId(threadLocal.get());
            productFullReductionMapper.insert(fullReduction);
        });
        log.info("当前的线程号...{}当前线程...{}", Thread.currentThread().getId(), Thread.currentThread().getName());

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveProductAttributeValue(PmsProductParam productParam) {
        for (ProductAttributeValue productAttributeValue : productParam.getProductAttributeValueList()) {
            productAttributeValue.setProductId(threadLocal.get());
            productAttributeValueMapper.insert(productAttributeValue);
        }
        log.info("当前的线程号...{}当前线程...{}", Thread.currentThread().getId(), Thread.currentThread().getName());

    }


}
