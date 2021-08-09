package com.example.msacquisitionbank.handler;

import com.example.msacquisitionbank.models.entities.Acquisition;
import com.example.msacquisitionbank.models.entities.Customer;
import com.example.msacquisitionbank.models.entities.Product;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

    public Mono<ServerResponse> findByProductId(ServerRequest request){
        String productId = request.pathVariable("productId");
        return productService.findByProductId(productId).flatMap(p -> ServerResponse.ok()
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

    public Mono<ServerResponse> createAcquisitionTest2(ServerRequest request){
        Mono<Acquisition> acquisition = request.bodyToMono(Acquisition.class);
        Acquisition acquisitionInit = new Acquisition();
        return acquisition.flatMap(acquisition1 -> productService.findByProductName(acquisition1.getProduct().getProductName())
                        .flatMap(product -> {
                            acquisitionInit.setProduct(product);
                            return Mono.just(acquisition1);
                        }).flatMap(acquisition2 -> Flux.fromIterable(acquisition2.getCustomerHolder())
                        .flatMap(customer -> customerService.findByIdentityNumber(customer.getCustomerIdentityNumber()))
                        .collectList()).flatMap(customers -> {
                            acquisitionInit.setCustomerHolder(customers);
                            acquisitionInit.setInitial(acquisition1.getInitial());
                            acquisitionInit.setCardNumber(acquisition1.getCardNumber());
                            acquisitionInit.setCustomerAuthorizedSigner(new ArrayList<>());
                            return Mono.just(acquisitionInit);
                        })
                        .flatMap(acquisition3 -> {
                            long quantityHolder = acquisition3.getCustomerHolder().size();
                            long quantityEnterpriseHolder = acquisition3.getCustomerHolder()
                                    .stream().filter(ce -> ce.getCustomerType().equals("ENTERPRISE")).count();
                            long quantityPersonalHolder = acquisition3.getCustomerHolder()
                                    .stream().filter(ce -> ce.getCustomerType().equals("PERSONAL")).count();
                            boolean isEnterprise=false;
                            boolean isPersonal=false;
                            if (quantityHolder > 1){
                                isPersonal = quantityPersonalHolder == quantityHolder;
                                if(isPersonal) {
                                    return Mono.just(ServerResponse.ok()
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .bodyValue("There should only be a maximum of 1 holder for personal clients."));
                                }
                            }
                            if (isPersonal){
                                acquisitionService
                                        .findAll()
                                        .collectList()
                                        .flatMap(acquisitionsPersonal -> {
                                            int i = 0;
                                            log.info("QUANTITY PRODUCT {}", i);
                                            for (Acquisition acquisition4 : acquisitionsPersonal){
                                                for (Customer customer : acquisition4.getCustomerHolder()){
                                                    for (Customer customer1 : acquisition3.getCustomerHolder()){
                                                        if (customer.getCustomerIdentityNumber().equals(customer1.getCustomerIdentityNumber())
                                                                && acquisition4.getProduct().getProductName().equals(acquisition4.getProduct().getProductName())
                                                        ) {
                                                            i++;
                                                        }
                                                    }
                                                }
                                            }
                                            log.info("QUANTITY PRODUCT after {}", i);
                                            if (i > 0){
                                                return Mono.just(ServerResponse.ok()
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .bodyValue("The customer already has the product: " + acquisition3.getProduct().getProductName()));
                                            }
                                            return Mono.just(acquisition3);
                                        });
                            }
                            return Mono.just(acquisition3);
                        })
                        .flatMap(acquisitionResponse -> acquisitionService.create(acquisitionInit)))
                .flatMap(response -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(response));
    }

    public static String generateRandom() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        sb.append(random.nextInt(9) + 1);
        for (int i = 0; i < 11; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
