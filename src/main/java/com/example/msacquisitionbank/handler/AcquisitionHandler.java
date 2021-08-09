package com.example.msacquisitionbank.handler;

import com.example.msacquisitionbank.models.entities.Acquisition;
import com.example.msacquisitionbank.services.BillService;
import com.example.msacquisitionbank.services.CustomerService;
import com.example.msacquisitionbank.services.IAcquisitionService;
import com.example.msacquisitionbank.services.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@Slf4j(topic = "ACQUISITION_HANDLER")
public class AcquisitionHandler {
    private final IAcquisitionService acquisitionService;
    private final BillService billService;
    private final ProductService productService;
    private final CustomerService customerService;
    @Autowired
    public AcquisitionHandler(IAcquisitionService acquisitionService, BillService billService, ProductService productService, CustomerService customerService) {
        this.acquisitionService = acquisitionService;
        this.billService = billService;
        this.productService = productService;
        this.customerService = customerService;
    }

    public Mono<ServerResponse> findAll(ServerRequest request){
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .body(acquisitionService.findAll(), Acquisition.class);
    }

    public Mono<ServerResponse> findById(ServerRequest request){
        String id = request.pathVariable("id");
        return acquisitionService.findById(id).flatMap(acquisition -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(acquisition))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> findByProductName(ServerRequest request){
        String productName = request.pathVariable("productName");
        return productService.findByProductName(productName).flatMap(p -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(p))
                .switchIfEmpty(Mono.error(new RuntimeException("THE PRODUCT DOES NOT EXIST")));
    }
    public Mono<ServerResponse> findByIdentityNumber(ServerRequest request){
        String identityNumber = request.pathVariable("identityNumber");
        return customerService.findByIdentityNumber(identityNumber).flatMap(p -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(p))
                .switchIfEmpty(Mono.error(new RuntimeException("THE PRODUCT DOES NOT EXIST")));
    }
    /*public Mono<ServerResponse> createAcquisition(ServerRequest request){
        Mono<Acquisition> acquisition = request.bodyToMono(Acquisition.class);
        return retire.flatMap(retireRequest ->  billService.findByAccountNumber(retireRequest.getBill().getAccountNumber())
                        .flatMap(billR -> {
                            billR.setBalance(billR.getBalance() - retireRequest.getAmount());
                            *//*if (retireRequest.getAmount() > billR.getBalance()){
                                return Mono.error(new RuntimeException("The retire amount exceeds the available balance"));
                            }*//*
                            return billService.updateBill(billR);
                        })
                        .flatMap(bilTransaction -> {
                            Transaction transaction = new Transaction();
                            transaction.setTransactionType("RETIRE");
                            transaction.setTransactionAmount(retireRequest.getAmount());
                            transaction.setBill(bilTransaction);
                            transaction.setDescription("RETIRE FROM THE CASHIER");
                            return transactionService.createTransaction(transaction);
                        })
                        .flatMap(currentTransaction -> {
                            retireRequest.setBill(currentTransaction.getBill());
                            return retireService.create(retireRequest);
                        })).flatMap(retireUpdate -> ServerResponse.created(URI.create("/retire/".concat(retireUpdate.getId())))
                        .contentType(APPLICATION_JSON)
                        .bodyValue(retireUpdate))
                .onErrorResume(e -> Mono.error(new RuntimeException("Error update retire")));
    }*/
}
