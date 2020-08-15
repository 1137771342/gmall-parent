package com.atguigu.gmall.vo.cart;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Project_Name gmall-parent
 * @Package_Name com.atguigu.gmall.cart
 * @Author yong Huang
 * @date 2020/8/15   19:21
 * 购物车中的每一条购物信息
 */
@Setter
public class CartItem implements Serializable {

    @Getter
    private Long skuId;//skuId
    //当前购物项的基本信息
    @Getter
    private String name;
    @Getter
    private String skuCode;
    @Getter
    private Integer stock;
    @Getter
    private String sp1;
    @Getter
    private String sp2;
    @Getter
    private String sp3;
    @Getter
    private String pic;

    @Getter
    private BigDecimal price;
    /**
     * 商品促销价格
     */
    @Getter
    private BigDecimal promotionPrice;




    //以上是购物项的基本信息
    @Getter
    private boolean check = true;//购物项的选中状态
    @Getter
    private Integer count;//有多少个



    private BigDecimal totalPrice;//当前购物项总价

    public BigDecimal getTotalPrice() {
        BigDecimal bigDecimal = price.multiply(new BigDecimal(count.toString()));
        return bigDecimal;
    }


}
