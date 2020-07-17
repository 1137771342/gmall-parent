package com.atguigu.gmall.pms.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.pms.entity.ProductOperateLog;
import com.atguigu.gmall.pms.mapper.ProductOperateLogMapper;
import com.atguigu.gmall.pms.service.ProductOperateLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Component;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Lfy
 * @since 2019-05-08
 */
@Service
@Component
public class ProductOperateLogServiceImpl extends ServiceImpl<ProductOperateLogMapper, ProductOperateLog> implements ProductOperateLogService {

}
