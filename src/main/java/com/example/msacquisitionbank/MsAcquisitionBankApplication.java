package com.example.msacquisitionbank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import reactor.core.publisher.Hooks;

@EnableEurekaClient
@SpringBootApplication
public class MsAcquisitionBankApplication {

    public static void main(String[] args) {
        Hooks.onOperatorDebug();
        SpringApplication.run(MsAcquisitionBankApplication.class, args);
    }

}
