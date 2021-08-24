package com.example.msacquisitionbank.handler;

import com.example.msacquisitionbank.models.dto.AverageBalanceDTO;
import com.example.msacquisitionbank.models.dto.AverageDTO;
import com.example.msacquisitionbank.models.entities.*;
import com.example.msacquisitionbank.services.*;
import com.example.msacquisitionbank.utils.AccountNumberGenerator;
import com.example.msacquisitionbank.utils.IbanGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
@Slf4j(topic = "ACQUISITION_HANDLER")
public class AcquisitionHandler {

    private final IAcquisitionService acquisitionService;
    private final BillService billService;
    private final ProductService productService;
    private final CustomerService customerService;
    private final PaymentService paymentService;
    private final TransactionService transactionService;
    @Autowired
    public AcquisitionHandler(IAcquisitionService acquisitionService, BillService billService, ProductService productService, CustomerService customerService, PaymentService paymentService, TransactionService transactionService) {
        this.acquisitionService = acquisitionService;
        this.billService = billService;
        this.productService = productService;
        this.customerService = customerService;
        this.paymentService = paymentService;
        this.transactionService = transactionService;
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

    public Mono<ServerResponse> findByIban(ServerRequest request){
        String iban = request.pathVariable("iban");
        return acquisitionService.findByIban(iban).flatMap(acquisition -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(acquisition))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> findByBillAccountNumber(ServerRequest request){
        String accountNumber = request.pathVariable("accountNumber");
        return acquisitionService.findByBill_AccountNumber(accountNumber).flatMap(acquisition -> ServerResponse.ok()
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

    public Mono<ServerResponse> transactionAverage(ServerRequest request){
        String month = request.pathVariable("month");
        String accountNumber = request.pathVariable("accountNumber");
        return transactionService.transactionAverage(month, accountNumber).flatMap(p -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(p))
                .switchIfEmpty(Mono.error(new RuntimeException("THE TRANSACTION AVERAGE REPORT DOES NOT EXIST")));
    }

    public Mono<ServerResponse> findAllByCustomer(ServerRequest request) {
        String identityNumber = request.pathVariable("identityNumber");
        Mono<AverageBalanceDTO> averageDTO = Mono.just(new AverageBalanceDTO());

        Mono<List<Acquisition>> acquisitionFlux = customerService.findByIdentityNumber(identityNumber).flatMapMany(customer -> {
            List<Customer> customers = new ArrayList<>();
            customers.add(customer);
            return acquisitionService.findAllByCustomerHolder(customers);
        }).collectList();

        Mono<List<AverageDTO>> averageDTOMono = acquisitionFlux.flatMapMany(acquisition -> Flux.fromIterable(acquisition).flatMapSequential(acquisition1 -> {
            return transactionService.transactionAverage("8", acquisition1.getBill().getAccountNumber());
        })).collectList();

        return Flux.zip(averageDTO, averageDTOMono)
                .flatMapSequential(result -> {
                    List<AverageDTO> averageDTO1 = new ArrayList<>(result.getT2());
                    result.getT1().setAverage(averageDTO1);
                    return Flux.just(result.getT1());
                })
                .log()
                .collectList()
                .flatMap(p -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(p));
    }

    public Mono<ServerResponse> createBill(ServerRequest request){
        Mono<Bill> bill = request.bodyToMono(Bill.class);
        return bill.flatMap(p-> {
                    return billService.createBill(p);
                }).flatMap(p -> ServerResponse.created(URI.create("/bill/".concat(p.getAccountNumber())))
                        .contentType(APPLICATION_JSON)
                        .bodyValue(p))
                .onErrorResume(error -> {
                    WebClientResponseException errorResponse = (WebClientResponseException) error;
                    if(errorResponse.getStatusCode() == HttpStatus.BAD_REQUEST) {
                        return ServerResponse.badRequest()
                                .contentType(APPLICATION_JSON)
                                .bodyValue(errorResponse.getResponseBodyAsString());
                    }
                    return Mono.error(errorResponse);
                });
    }

    public Mono<ServerResponse> createAcquisitionTest2(ServerRequest request){
        Mono<Acquisition> acquisition = request.bodyToMono(Acquisition.class);
        Acquisition acquisitionInit = new Acquisition();
        AccountNumberGenerator accountNumberGenerator = new AccountNumberGenerator();
        String accNumber = accountNumberGenerator.generate(15);
        IbanGenerator ibanGenerator = new IbanGenerator();
        String ibanByAccount = ibanGenerator.generate(accNumber);
        return acquisition.flatMap(acquisition1 -> productService.findByProductName(acquisition1.getProduct().getProductName())
                        .flatMap(product -> {
                            acquisitionInit.setProduct(product);
                            return Mono.just(acquisition1);
                        }).flatMap(acquisition2 -> Flux.fromIterable(acquisition2.getCustomerHolder())
                        .flatMap(customer -> customerService.findByIdentityNumber(customer.getCustomerIdentityNumber()))
                        .collectList()).flatMap(customers -> {
                            acquisitionInit.setCustomerHolder(customers);
                            acquisitionInit.setInitial(acquisition1.getInitial());
                            acquisitionInit.setIban(ibanByAccount);
                            acquisitionInit.setCardNumber("");
                            acquisitionInit.setCustomerAuthorizedSigner(new ArrayList<>());
                            if (Objects.equals(acquisition1.getProduct().getProductName(), "TARJETA DE CREDITO")){
                                return paymentService.createPayment(Payment.builder()
                                        .acquisition(acquisitionInit)
                                        .creditLine(acquisitionInit.getInitial())
                                        .amount(acquisitionInit.getInitial())
                                        .description("Credit card payment")
                                        .expirationDate(LocalDateTime.now().plusDays(30))
                                        .build()).flatMap(acq -> Mono.just(acq.getAcquisition()));
                            }
                            return Mono.just(acquisitionInit);
                        })
                        .flatMap(acquisition3 -> {
                            long quantityHolder = acquisition3.getCustomerHolder().size();
                            long quantityEnterpriseHolder = acquisition3.getCustomerHolder()
                                    .stream().filter(ce -> ce.getCustomerType().equals("ENTERPRISE")).count();
                            long quantityPersonalHolder = acquisition3.getCustomerHolder()
                                    .stream().filter(ce -> ce.getCustomerType().equals("PERSONAL")).count();
                            boolean isEnterprise=false;
                            isEnterprise = quantityEnterpriseHolder==quantityHolder&&quantityPersonalHolder==0;

                            if (isEnterprise){
                                if (acquisition3.getProduct().getProductName().equals("PLAZO FIJO")
                                || acquisition3.getProduct().getProductName().equals("AHORRO")){
                                    return Mono.error(new RuntimeException(String.format("The business customer cannot have account product of type : %s", acquisition3.getProduct().getProductName())));
                                }
                                // si quiere creear una credito personal
                            }else{
                                return acquisitionService
                                        .findAll()
                                        .collectList()
                                        .flatMap(acquisitionsPersonal -> {
                                            int i = 0;
                                            for (Acquisition acquisition4 : acquisitionsPersonal){
                                                for (Customer customer : acquisition4.getCustomerHolder()){
                                                    for (Customer customer1 : acquisition3.getCustomerHolder()){
                                                        if (customer.getCustomerIdentityNumber().equals(customer1.getCustomerIdentityNumber())
                                                                && acquisition4.getProduct().getProductName().equals(acquisition3.getProduct().getProductName())
                                                        ) {
                                                            i++;
                                                        }
                                                    }
                                                }
                                            }

                                            if (i > 0){
                                                return Mono.empty();
                                            }

                                            long existCreditCard = acquisitionsPersonal.stream()
                                                    .filter(acquisition2 -> acquisition2.getCustomerHolder().equals(acquisition3.getCustomerHolder()))
                                                    .filter(acquisition2 -> Objects.equals(acquisition2.getProduct().getProductName(), "TARJETA DE CREDITO")).count();
                                            if (existCreditCard == 0 && (acquisition3.getProduct().getProductName().equals("CUENTA AHORRO VIP")
                                                        || acquisition3.getProduct().getProductName().equals("CUENTA CORRIENTE PYME"))){
                                                    return Mono.error(new RuntimeException("To request this product the customer must have a credit card"));
                                            }
                                            return Mono.just(acquisition3);
                                        }).switchIfEmpty(Mono.error(new RuntimeException(String.format("The client type personal has the %s account product", acquisition3.getProduct().getProductName()))));
                            }
                            return Mono.just(acquisition3);
                        })
                        //.doOnNext()
                        .checkpoint("after validation business rules for creation")
                        .flatMap(acquisitionBill -> {
                            if (acquisitionBill.getInitial() < 0){
                                return Mono.empty();
                            }
                            Bill bill = new Bill();
                            bill.setAccountNumber(accNumber);
                            bill.setAcquisition(acquisitionBill);
                            bill.setBalance(acquisitionBill.getInitial());
                            return billService.createBill(bill);
                        })
                        .checkpoint("after create bill web-client")
                        .switchIfEmpty(Mono.error(new RuntimeException("the initial amount must be greater than zero")))
                        .flatMap(bill -> {
                            acquisitionInit.setBill(bill);
                            return acquisitionService.create(acquisitionInit);
                        }))
                .switchIfEmpty(Mono.error(new RuntimeException("the acquisition cannot be empty")))
                .flatMap(response -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(response));
    }

    public Mono<ServerResponse> updateAcquisition(ServerRequest request){
        Mono<Acquisition> acquisition = request.bodyToMono(Acquisition.class);
        return acquisition.flatMap(acquisitionEdit -> acquisitionService.findByIban(acquisitionEdit.getIban()).flatMap(currentAcquisition -> {
            currentAcquisition.setProduct(acquisitionEdit.getProduct());
            currentAcquisition.setBill(acquisitionEdit.getBill());
            return acquisitionService.update(currentAcquisition);
        })).flatMap(acquisitionResponse -> ServerResponse.created(URI.create("/api/acquisition/".concat(acquisitionResponse.getId())))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(acquisitionResponse));
    }

    public Mono<ServerResponse> update(ServerRequest request){
        Mono<Acquisition> acquisition = request.bodyToMono(Acquisition.class);
        String iban = request.pathVariable("iban");
        Mono<Acquisition> acquisitionDB = acquisitionService.findByIban(iban);
        return acquisitionDB.zipWith(acquisition, (db, req) -> {
            db.setProduct(req.getProduct());
            db.setBill(req.getBill());
            return db;
        })
                .flatMap(acquisitionUpdate -> {
                    billService.updateBill(acquisitionUpdate.getBill());
                    return Mono.just(acquisitionUpdate);
                }).flatMap(acquisitionService::update).flatMap(acquisitionResponse -> ServerResponse.created(URI.create("/acquisition/".concat(acquisitionResponse.getId())))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(acquisitionResponse));
    }
}
