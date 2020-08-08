package com.atguigu.gmall.pms.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.constant.EsConstant;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.mapper.*;
import com.atguigu.gmall.pms.service.ProductService;
import com.atguigu.gmall.to.es.EsProduct;
import com.atguigu.gmall.to.es.EsProductAttributeValue;
import com.atguigu.gmall.to.es.EsSkuProductInfo;
import com.atguigu.gmall.vo.PageInfoVo;
import com.atguigu.gmall.vo.product.PmsProductParam;
import com.atguigu.gmall.vo.product.PmsProductQueryParam;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.searchbox.client.JestClient;
import io.searchbox.core.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
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

    @Autowired
    ProductAttributeMapper productAttributeMapper;

    @Autowired
    private JestClient jestClient;

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

    @Override
    public Product getProductInfo(Long id) {
        return productMapper.selectById(id);
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
        int i = 10 / 0;
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
        log.info("当前的线程号...{}当前线程...{}", Thread.currentThread().getId(),
                Thread.currentThread().getName());

    }


    /**
     * @param ids
     * @param publishStatus
     */
    @Override
    public void batchUpdatePublishStatus(List<Long> ids, Integer publishStatus) {
        if (publishStatus == 0) {
            //商品下架，从es中删除商品
            for (Long id : ids) {
                setProductPublishStatus(publishStatus, id);
                deleteProductFromEs(id);
            }

        } else {
            //商品上架，保存商品到es
            for (Long id : ids) {
                setProductPublishStatus(publishStatus, id);
                saveProductToEs(id);
            }
        }


    }

    /**
     * 从es查询获取商品详情
     *
     * @param id
     * @return
     */
    @Override
    public EsProduct productAllInfo(Long id) {
        EsProduct esProduct = null;
        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.query(QueryBuilders.termQuery("id", id));
        Search build = new Search.Builder(builder.toString())
                .addIndex(EsConstant.PRODUCT_ES_INDEX)
                .addType(EsConstant.PRODUCT_TYPE)
                .build();
        try {
            SearchResult result = jestClient.execute(build);
            List<SearchResult.Hit<EsProduct, Void>> hits = result.getHits(EsProduct.class);
            esProduct = hits.get(0).source;
        } catch (IOException e) {
        }
        return esProduct;
    }

    /**
     * 查询商品的sku信息
     *
     * @param id
     * @return
     */
    @Override
    public EsProduct productSkuInfo(Long id) {
        EsProduct esProduct = null;
        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.query(QueryBuilders.nestedQuery("skuProductInfos",
                QueryBuilders.termQuery("skuProductInfos.id", id), ScoreMode.None));

        Search build = new Search.Builder(builder.toString()).addIndex(EsConstant.PRODUCT_ES_INDEX)
                .addType(EsConstant.PRODUCT_TYPE).build();
        try {
            SearchResult result = jestClient.execute(build);
            List<SearchResult.Hit<EsProduct, Void>> hits = result.getHits(EsProduct.class);
            esProduct = hits.get(0).source;
        } catch (IOException e) {
        }
        return esProduct;

    }

    private void setProductPublishStatus(Integer publishStatus, Long id) {
        Product product = new Product();
        product.setId(id);
        product.setPublishStatus(publishStatus);
        productMapper.updateById(product);
    }

    private void deleteProductFromEs(Long id) {
        Delete builder = new Delete.Builder(id.toString())
                .index(EsConstant.PRODUCT_ES_INDEX)
                .type(EsConstant.PRODUCT_TYPE).build();
        try {
            DocumentResult execute = jestClient.execute(builder);
            if (execute.isSucceeded()) {
                log.info("商品id：{} ==> es下架成功", id);
            } else {
                log.error("商品：{} ==》ES下架失败", id);
            }
        } catch (IOException e) {
            log.error("商品：{} ==》ES下架失败", id);
        }
    }

    private void saveProductToEs(Long id) {
        //商品的基础数据
        //商品的批量上下架操作,首先我们要将上架的商品存在es中
        Product productInfo = getProductInfo(id);
        EsProduct esProduct = new EsProduct();
        BeanUtils.copyProperties(productInfo, esProduct);
        //todo 保存商品的spu信息 EsProductAttributeValue
        List<EsProductAttributeValue> attributeValues = productAttributeValueMapper.selectProductBaseAttrAndValue(id);
        esProduct.setAttrValueList(attributeValues);

        List<SkuStock> stocks = skuStockMapper.selectList(new QueryWrapper<SkuStock>().eq("product_id", id));
        List<EsSkuProductInfo> esSkuProductInfos = new ArrayList<>();
        //todo 保存商品的sku信息 EsSkuProductInfo
        List<ProductAttribute> skuAttributeNames = productAttributeMapper.selectProductSaleAttrName(id);

        stocks.forEach(skuStock -> {
            EsSkuProductInfo info = new EsSkuProductInfo();
            //复制基本的sku属性
            BeanUtils.copyProperties(skuStock, info);

            //设置skutitle
            String skuTitle = esProduct.getName();
            if (StringUtils.isNotBlank(skuStock.getSp1())) {
                skuTitle += " " + skuStock.getSp1();
            }
            if (StringUtils.isNotBlank(skuStock.getSp2())) {
                skuTitle += " " + skuStock.getSp2();
            }
            if (StringUtils.isNotBlank(skuStock.getSp3())) {
                skuTitle += " " + skuStock.getSp3();
            }
            info.setSkuTitle(skuTitle);
            List<EsProductAttributeValue> skuAttributeValues = new ArrayList<>();
            for (int i = 0; i < skuAttributeNames.size(); i++) {
                EsProductAttributeValue value = new EsProductAttributeValue();
                value.setProductAttributeId(skuAttributeNames.get(i).getId());
                value.setName(skuAttributeNames.get(i).getName());
                value.setType(skuAttributeNames.get(i).getType());
                value.setProductId(id);
                //设置value属性
                if (i == 0) {
                    value.setValue(skuStock.getSp1());
                }
                if (i == 1) {
                    value.setValue(skuStock.getSp2());
                }
                if (i == 2) {
                    value.setValue(skuStock.getSp3());
                }
                skuAttributeValues.add(value);
            }
            info.setAttributeValues(skuAttributeValues);
            esSkuProductInfos.add(info);
        });
        esProduct.setSkuProductInfos(esSkuProductInfos);
        //将商品保存在es中
        Index build = new Index.Builder(esProduct).index(EsConstant.PRODUCT_ES_INDEX).type(EsConstant.PRODUCT_TYPE).
                build();
        try {
            DocumentResult execute = jestClient.execute(build);
            boolean succeeded = execute.isSucceeded();
            if (succeeded) {
                log.info("ES中；id为{}商品上架完成", id);
            } else {
                log.info("ES中；id为{}商品未保存成功,开始重试", id);
            }
        } catch (IOException e) {
            log.error("ES中；id为{}商品数据保存异常；{}", id, e.getMessage());
        }
    }


}
