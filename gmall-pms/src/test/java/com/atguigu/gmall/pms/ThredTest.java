package com.atguigu.gmall.pms;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Project_Name gmall-parent
 * @Package_Name com.atguigu.gmall.pms
 * @Author yong Huang
 * @date 2020/8/8   18:28
 */
public class ThredTest {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService threadPool = Executors.newFixedThreadPool(10);

        CompletableFuture<String> f1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品的基本数据");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "小米";
        }, threadPool).whenComplete((r,e)->{
            System.out.println("上一步运行的结果是: "+r);
        });

        CompletableFuture<Integer> f2 = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品的属性是...");
            return 1;
        }, threadPool).whenComplete((r, e) -> {
            System.out.println("查询属性获得的结果是" + r);
        });

        CompletableFuture<String> f3 = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品营销数据...");
            return "满199减100";
        }, threadPool).whenComplete((r,e)->{
            System.out.println("结果是："+r);
        });

        CompletableFuture<Void> allOf = CompletableFuture.allOf(f1, f2, f3);


    }
}
