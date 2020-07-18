package com.atguigu.gmall.admin.aop;

import com.atguigu.gmall.to.CommonResult;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

/**
 * @Project_Name gmall-parent
 * @Package_Name com.atguigu.gmall.admin.aop
 * @Author yong Huang
 * @date 2020/7/18   10:22
 * 数据校验切面
 */
@Aspect
@Component
@Slf4j
public class DataValidatorAspect {


    @Around("execution(* com.atguigu.gmall.admin..*Controller.*(..))")
    public Object validator(ProceedingJoinPoint pjp) {
        Object proceed = null;
        try {
            Object[] args = pjp.getArgs();
            for (Object arg : args) {
                if (arg instanceof BindingResult) {
                    BindingResult obj = (BindingResult) arg;
                    if (obj.getErrorCount() > 0) {
                        return new CommonResult().validateFailed(obj);
                    }
                }
            }
            proceed = pjp.proceed(pjp.getArgs());
        } catch (Throwable throwable) {
            log.error("参数校验切面异常 {}",throwable);
            throw new RuntimeException(throwable);
        } finally {

        }
        return proceed;
    }
}
