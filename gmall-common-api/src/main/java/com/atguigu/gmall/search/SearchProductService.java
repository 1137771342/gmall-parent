package com.atguigu.gmall.search;

import com.atguigu.gmall.vo.search.SearchParam;
import com.atguigu.gmall.vo.search.SearchResponse;

/**
 * @Project_Name gmall-parent
 * @Package_Name com.atguigu.gmall.search
 * @Author yong Huang
 * @date 2020/8/1   14:45
 */
public interface SearchProductService {
    /**
     * 商品检索
     * @param searchParam
     * @return
     */
    SearchResponse searchProduct(SearchParam searchParam);
}
