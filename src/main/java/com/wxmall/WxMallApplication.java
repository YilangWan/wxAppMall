package com.wxmall;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@MapperScan("com.wxmall.mapper")
@EnableCaching
public class WxMallApplication {

    public static void main(String[] args) {
        SpringApplication.run(WxMallApplication.class, args);
    }
}
