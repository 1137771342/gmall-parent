package com.atguigu.gamll.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.search.SearchProductService;
import com.atguigu.gmall.vo.search.SearchParam;
import com.atguigu.gmall.vo.search.SearchResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品检索的controller
 * @author 86135
 */
@Api(tags = "检索功能")
@RestController
public class ProductSearchController {


    @Reference
    SearchProductService searchProductService;


    /**
     * 检索商品
     */
    @ApiOperation("商品检索")
    @GetMapping("/search")
    public SearchResponse productSearchResponse(SearchParam searchParam){
        SearchResponse searchResponse = searchProductService.searchProduct(searchParam);
        return searchResponse;
    }

}
