package com.atguigu.gmall.pms.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.constant.SysCacheConstant;
import com.atguigu.gmall.pms.entity.ProductCategory;
import com.atguigu.gmall.pms.mapper.ProductCategoryMapper;
import com.atguigu.gmall.pms.service.ProductCategoryService;
import com.atguigu.gmall.vo.product.PmsProductCategoryWithChildrenItem;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <p>
 * 产品分类 服务实现类
 * </p>
 *
 * @author Lfy
 * @since 2019-05-08
 */
@Slf4j
@Service
@Component
public class ProductCategoryServiceImpl extends ServiceImpl<ProductCategoryMapper, ProductCategory> implements ProductCategoryService {

    @Autowired
    ProductCategoryMapper productCategoryMapper;

    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public List<PmsProductCategoryWithChildrenItem> listCategoryWithChildren(Integer id) {
        Object obj = redisTemplate.opsForValue().get(SysCacheConstant.CATEGORY_CACHE_KEY);
        List<PmsProductCategoryWithChildrenItem> items = null;
        if (obj == null) {
            //查询数据库
            items = productCategoryMapper.listCategoryWithChildren(id);
            //将值放入缓存中
            redisTemplate.opsForValue().set(SysCacheConstant.CATEGORY_CACHE_KEY, items);
        } else {
            log.info("商品分类查询命中缓存");
            items = (List<PmsProductCategoryWithChildrenItem>) obj;
        }
        return items;
    }
}
