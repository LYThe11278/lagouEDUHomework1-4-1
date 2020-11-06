package com.lagouedu.zkhomework;

import com.lagouedu.zkhomework.utils.ZkTest1;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ZkhomeworkApplication {
    public static void main(String[] args) throws InterruptedException {

        SpringApplication.run(ZkhomeworkApplication.class, args);
        ZkTest1.initZk();
    }
}
