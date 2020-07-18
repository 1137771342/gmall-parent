package com.atguigu.gmall.admin.handler;

import com.atguigu.gmall.to.CommonResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @Project_Name gmall-parent
 * @Package_Name com.atguigu.gmall.admin.handler
 * @Author yong Huang
 * @date 2020/7/18   10:49
 * 全局异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Object globalException(Exception e) {
        log.error("系统出现了全局异常", e.getStackTrace());
        return new CommonResult().failed();
    }
}
