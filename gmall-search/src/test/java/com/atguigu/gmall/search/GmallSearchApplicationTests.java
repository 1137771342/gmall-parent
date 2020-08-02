package com.atguigu.gmall.search;

import com.atguigu.gmall.vo.search.SearchParam;
import org.assertj.core.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallSearchApplicationTests {

    @Autowired
    private SearchProductService searchProductService;

    @Test
    public void dslTest() {
        SearchParam searchParam=new SearchParam();
        searchParam.setKeyword("手机");
        searchParam.setBrand(Arrays.array("苹果"));
        searchParam.setCatelog3(Arrays.array("19"));
        searchParam.setPriceFrom(5000);
        searchParam.setPriceTo(10000);
        searchParam.setProps(Arrays.array("45:4.7","46:4G"));
        searchProductService.searchProduct(searchParam);

    }

}
