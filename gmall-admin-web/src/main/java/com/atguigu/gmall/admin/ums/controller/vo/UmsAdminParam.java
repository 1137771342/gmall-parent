package com.atguigu.gmall.admin.ums.controller.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**
 * 用户登录参数
 * Created by atguigu 4/26.
 */
@ToString
@Getter
@Setter
public class UmsAdminParam {

    /**
     * 能使用的校验注解
     * 1）、Hibernate   org.hibernate.validator.constraints 里面的所有
     * 2）、JSR303规范规定的都可；
     * javax.validation.constraints
     *
     * @Pattern(regexp = "")
     */
    @Length(min = 6, max = 18, message = "用户名长度必须是6-18位")
    @NotNull(message = "用户名不能为空")
    @ApiModelProperty(value = "用户名", required = true)
    private String username;


    @ApiModelProperty(value = "密码", required = true)
    private String password;

    @ApiModelProperty(value = "用户头像")
    private String icon;

    @Email(message = "邮箱格式不正确，哈哈哈")
    @NotNull(message = "邮箱不能为空")
    @ApiModelProperty(value = "邮箱")
    private String email;

    //使用负责正则进行校验
    @ApiModelProperty(value = "用户昵称")
    private String nickName;


    @ApiModelProperty(value = "备注")
    private String note;
}
