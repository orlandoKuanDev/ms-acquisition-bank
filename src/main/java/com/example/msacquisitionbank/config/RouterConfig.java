package com.example.msacquisitionbank.config;

import com.example.msacquisitionbank.handler.AcquisitionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterConfig {
    @Bean
    public RouterFunction<ServerResponse> rutas(AcquisitionHandler handler){
        return route(GET("/acquisition"), handler::findAll)
                .andRoute(GET("/acquisition/{id}"), handler::findById)
                .andRoute(GET("/acquisition/product/{productName}"), handler::findByProductName)
                .andRoute(GET("/acquisition/productId/{productId}"), handler::findByProductId)
                .andRoute(GET("/acquisition/customer/{identityNumber}"), handler::findByIdentityNumber)
                .andRoute(GET("/acquisition/bill/{accountNumber}"), handler::findByBillAccountNumber)
                .andRoute(GET("/acquisition/all/{identityNumber}"), handler::findAllByCustomer)
                .andRoute(GET("/acquisition/card/{iban}"), handler::findByIban)
                .andRoute(GET("/acquisition/bills/{accountNumber}"), handler::findByBillAccountNumber)
                .andRoute(GET("/acquisition/transaction/average/{month}/{accountNumber}"), handler::transactionAverage)
                .andRoute(POST("/acquisition/bill/"), handler::createBill)
                .andRoute(POST("/acquisition/create"), handler::createAcquisitionTest2)
                .andRoute(POST("/acquisition/update"), handler::updateAcquisition)
                .andRoute(PUT("/acquisition/update/{iban}"), handler::update);
    }
}