package com.atguigu.gmall.search;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.constant.EsConstant;
import com.atguigu.gmall.constant.SysConstant;
import com.atguigu.gmall.to.es.EsProduct;
import com.atguigu.gmall.vo.search.SearchParam;
import com.atguigu.gmall.vo.search.SearchResponse;
import com.atguigu.gmall.vo.search.SearchResponseAttrVo;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Project_Name gmall-parent
 * @Package_Name com.atguigu.gmall.search
 * @Author yong Huang
 * @date 2020/8/1   14:41
 */
@Service
@Component
@Slf4j
public class SearchProductServiceImpl implements SearchProductService {

    @Autowired
    private JestClient jestClient;


    @Override
    public SearchResponse searchProduct(SearchParam searchParam) {
        //构建检索条件dsl语句
        String dsl = buildSearchDsl(searchParam);
        log.info("{}", JSON.toJSONString(dsl));
        Search search = new Search.Builder(dsl)
                .addIndex(EsConstant.PRODUCT_ES_INDEX)
                .addType(EsConstant.PRODUCT_TYPE).build();
        SearchResult execute = null;
        //执行检索
        try {
            execute = jestClient.execute(search);

        } catch (IOException e) {
            e.printStackTrace();
        }

        //将检索封装成SearchResponse对象
        SearchResponse searchResponse = buildSearchResponse(execute);
        searchResponse.setPageNum(searchParam.getPageNum());
        searchResponse.setPageSize(searchParam.getPageSize());
        return searchResponse;
    }


    /**
     * 构建检索条件dsl语句
     *
     * @param searchParam
     * @return
     */
    private String buildSearchDsl(SearchParam searchParam) {
        SearchSourceBuilder builder = new SearchSourceBuilder();
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        //1.查询
        //1.1检索
        if (StringUtils.isNotBlank(searchParam.getKeyword())) {
            boolQuery.must(
                    QueryBuilders.nestedQuery("skuProductInfos",
                            QueryBuilders.matchQuery("skuProductInfos.skuTitle", searchParam.getKeyword()),
                            ScoreMode.None));
        }
        //1.2过滤
        //1.2.1 品牌过滤
        if (searchParam.getBrand() != null && searchParam.getBrand().length > 0) {
            boolQuery.filter(QueryBuilders.termsQuery("brandName.keyword", searchParam.getBrand()));
        }
        //1.2.2 分类过滤
        if (searchParam.getCatelog3() != null && searchParam.getCatelog3().length > 0) {
            boolQuery.filter(QueryBuilders.termsQuery("productCategoryId", searchParam.getCatelog3()));
        }
        //1.2.3 属性过滤
        if (searchParam.getProps() != null && searchParam.getProps().length > 0) {
            String[] props = searchParam.getProps();
            for (String prop : props) {
                String[] split = prop.split(":");
                //2:4g-3g； 2号属性的值是4g或者3g
                boolQuery.filter(QueryBuilders.nestedQuery("attrValueList",
                        QueryBuilders.boolQuery()
                                .must(QueryBuilders.matchQuery("attrValueList.productAttributeId", split[0]))
                                .must(QueryBuilders.termsQuery("attrValueList.value.keyword", split[1].split("-"))),
                        ScoreMode.None));
            }
        }
        //1.2.4 价格区间过滤
        if (searchParam.getPriceFrom() != null || searchParam.getPriceTo() != null) {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("price");
            if (searchParam.getPriceFrom() != null) {
                rangeQuery.gte(searchParam.getPriceFrom());
            }
            if (searchParam.getPriceTo() != null) {
                rangeQuery.lte(searchParam.getPriceTo());
            }
            boolQuery.filter(rangeQuery);
        }
        builder.query(boolQuery);
        //2.聚合
        //2.1品牌聚合
        TermsAggregationBuilder brandAgg = AggregationBuilders.terms("brand_agg").field("brandName.keyword")
                .subAggregation(AggregationBuilders.terms("brandId").field("brandId"));
        builder.aggregation(brandAgg);

        //2.2分类聚合
        TermsAggregationBuilder categoryAgg = AggregationBuilders.terms("category_agg").field("productCategoryName.keyword")
                .subAggregation(AggregationBuilders.terms("categoryId_agg").field("productCategoryId"));
        builder.aggregation(categoryAgg);
        //2.3 属性聚合
        NestedAggregationBuilder attrAgg = AggregationBuilders.nested("attr_agg", "attrValueList");
        TermsAggregationBuilder attrNameAgg = AggregationBuilders.terms("attrName_agg").field("attrValueList.name");
        //聚合看attrValue的值
        attrNameAgg.subAggregation(AggregationBuilders.terms("attrValue_agg").field("attrValueList.value.keyword"));
        //聚合看attrId的值
        attrNameAgg.subAggregation(AggregationBuilders.terms("attrId_agg").field("attrValueList.productAttributeId"));
        attrAgg.subAggregation(attrNameAgg);
        builder.aggregation(attrAgg);
        //3.设置高亮
        if (StringUtils.isNotBlank(searchParam.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuProductInfos.skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            builder.highlighter(highlightBuilder);
        }

        //4.分页
        builder.from((searchParam.getPageNum() - 1) * searchParam.getPageSize());
        builder.size(searchParam.getPageSize());
        //5.排序 0：综合排序  1：销量  2：价格
        if (StringUtils.isNotBlank(searchParam.getOrder())) {
            String order = searchParam.getOrder();
            String[] split = order.split(":");
            if ("0".equals(split[0])) {
                //综合排序,不进行操作
            } else if ("1".equals(split[0])) {
                FieldSortBuilder sale = SortBuilders.fieldSort("sale");
                //销量排序
                if (SysConstant.ASC.equalsIgnoreCase(split[1])) {
                    sale.order(SortOrder.ASC);
                } else {
                    sale.order(SortOrder.DESC);
                }
                builder.sort(sale);
            } else if ("2".equals(split[0])) {
                FieldSortBuilder price = SortBuilders.fieldSort("price");
                //价格排序
                if (SysConstant.ASC.equalsIgnoreCase(split[1])) {
                    price.order(SortOrder.ASC);
                } else {
                    price.order(SortOrder.DESC);
                }
                builder.sort(price);
            }
        }
        return builder.toString();
    }

    /**
     * 返回前端结果封装
     *
     * @param execute
     * @return
     */
    private SearchResponse buildSearchResponse(SearchResult execute) {
        SearchResponse searchResponse = new SearchResponse();
        MetricAggregation aggregations = execute.getAggregations();
        //品牌
        List<String> brandNames = new ArrayList<>();
        aggregations.getTermsAggregation("brand_agg").getBuckets().forEach(bucket -> {
            String brandName = bucket.getKeyAsString();
            brandNames.add(brandName);
        });
        SearchResponseAttrVo brandVo = new SearchResponseAttrVo();
        brandVo.setName("品牌");
        brandVo.setValue(brandNames);
        searchResponse.setBrand(brandVo);
        //获取分类
        TermsAggregation categoryAgg = aggregations.getTermsAggregation("category_agg");
        List<String> categoryValues = new ArrayList<>();
        Map<String, Object> map = new HashMap();
        categoryAgg.getBuckets().forEach(bucket -> {
            String categoryName = bucket.getKeyAsString();
            TermsAggregation categoryIdAgg = bucket.getTermsAggregation("categoryId_agg");
            String categoryId = categoryIdAgg.getBuckets().get(0).getKeyAsString();
            map.put("id", categoryId);
            map.put("name", categoryName);
            categoryValues.add(JSON.toJSONString(map));
        });
        SearchResponseAttrVo categoryVo = new SearchResponseAttrVo();
        categoryVo.setName("分类");
        categoryVo.setValue(categoryValues);
        searchResponse.setCatelog(categoryVo);
        //获取属性
        TermsAggregation attrNameAgg = aggregations.getChildrenAggregation("attr_agg").
                getTermsAggregation("attrName_agg");
        List<SearchResponseAttrVo> attrVos = new ArrayList<>();
        attrNameAgg.getBuckets().forEach(bucket -> {
            SearchResponseAttrVo vo = new SearchResponseAttrVo();
            vo.setName(bucket.getKeyAsString());
            //属性id
            TermsAggregation attrIdAgg = bucket.getTermsAggregation("attrId_agg");
            vo.setProductAttributeId(Long.parseLong(attrIdAgg.getBuckets().get(0).getKeyAsString()));
            //属性值
            TermsAggregation attrValueAgg = bucket.getTermsAggregation("attrValue_agg");
            List<String> valueList = new ArrayList<>();
            attrValueAgg.getBuckets().forEach(valueBucket -> {
                //设置名称
                String attrValue = valueBucket.getKeyAsString();
                valueList.add(attrValue);
            });
            vo.setValue(valueList);
            attrVos.add(vo);
        });
        searchResponse.setAttrs(attrVos);

        //获取商品
        List<SearchResult.Hit<EsProduct, Void>> hits = execute.getHits(EsProduct.class);
        List<EsProduct> esProducts = new ArrayList<>();
        hits.forEach(hit -> {
            EsProduct source = hit.source;
            String title = hit.highlight.get("skuProductInfos.skuTitle").get(0);
            source.setName(title);
            esProducts.add(source);
        });
        searchResponse.setProducts(esProducts);
        searchResponse.setTotal(execute.getTotal());
        return searchResponse;
    }
}
