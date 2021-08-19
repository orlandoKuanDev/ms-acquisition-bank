package com.example.msacquisitionbank.services;

import com.example.msacquisitionbank.models.entities.Acquisition;
import com.example.msacquisitionbank.models.entities.Customer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IAcquisitionService extends IBaseService<Acquisition, String>{
    Mono<List<Acquisition>> findByCustomerIdentityNumber(String identityNumber);
    Mono<Acquisition> findByBill_AccountNumber(String accountNumber);
    Mono<Acquisition> findByIban(String iban);
    Flux<Acquisition> findAllByCustomerHolder(List<Customer> customers);
}
