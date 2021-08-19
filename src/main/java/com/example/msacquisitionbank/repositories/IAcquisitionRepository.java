package com.example.msacquisitionbank.repositories;

import com.example.msacquisitionbank.models.entities.Acquisition;
import com.example.msacquisitionbank.models.entities.Customer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IAcquisitionRepository extends IRepository<Acquisition, String>{
    Mono<Acquisition> findByIban(String iban);
    Mono<Acquisition> findByBill_AccountNumber(String accountNumber);
    Flux<Acquisition> findAllByCustomerHolder(List<Customer> customers);
}
