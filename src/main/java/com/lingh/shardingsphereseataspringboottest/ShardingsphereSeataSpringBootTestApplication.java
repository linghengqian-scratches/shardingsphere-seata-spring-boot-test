package com.lingh.shardingsphereseataspringboottest;

import io.seata.spring.boot.autoconfigure.SeataAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = SeataAutoConfiguration.class)
public class ShardingsphereSeataSpringBootTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShardingsphereSeataSpringBootTestApplication.class, args);
    }

}
