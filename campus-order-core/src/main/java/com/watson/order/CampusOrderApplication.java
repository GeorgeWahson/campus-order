package com.watson.order;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j
@EnableTransactionManagement
@SpringBootApplication
@EnableCaching
public class CampusOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusOrderApplication.class, args);
        log.info("=============项目启动=============");
    }

}
