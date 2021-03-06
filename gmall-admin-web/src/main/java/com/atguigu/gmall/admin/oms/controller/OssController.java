package com.atguigu.gmall.admin.oms.controller;


import com.atguigu.gmall.admin.oms.component.OssCompent;
import com.atguigu.gmall.to.CommonResult;
import com.atguigu.gmall.to.OssPolicyResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * Oss相关操作接口
 *
 * 1、阿里云上传
 * 		前端页面form表单文档上传--->后台（收到文件流）--->ossClient.upload到阿里云
 *
 * 		前端传视频；
 *
 *
 * 2、如果要配置成自己的阿里云
 * 		1）、前端项目里面搜索leifengyang 改成自己的地址
 * 		2）、application.properties 复制成自己相关值
 * 		3）、开启阿里云oss的跨域访问
 *
 */
//@CrossOrigin(origins = "www.baidu.com")
@CrossOrigin
@Controller
@Api(tags = "OssController",description = "Oss管理")
@RequestMapping("/aliyun/oss")
public class OssController {
	@Autowired
	private OssCompent ossComponent;

	@ApiOperation(value = "oss上传签名生成")
	@GetMapping(value = "/policy")
	@ResponseBody
	public Object policy() {
		OssPolicyResult policy = ossComponent.policy();
		return new CommonResult().success(policy);
	}

}
