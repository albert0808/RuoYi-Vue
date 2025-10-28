package com.albert.learning;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//@SpringBootApplication(scanBasePackages = {
//        "com.albert.learning" // 扫描你自己模块的代码
//})
@SpringBootApplication
public class LeanApplication {
    public static void main(String[] args) {
        SpringApplication.run(LeanApplication.class,args);
        System.out.println("✅ LeanApplication 启动成功！");
    }
}
