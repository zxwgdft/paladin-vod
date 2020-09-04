package com.paladin.vod;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @author TontoZhou
 * @since 2019/10/30
 */
@MapperScan(basePackages = "com.paladin.vod.mapper")
@SpringBootApplication
public class VodApplication {

    public static void main(String[] args) {
        SpringApplication.run(VodApplication.class, args);
    }

}
