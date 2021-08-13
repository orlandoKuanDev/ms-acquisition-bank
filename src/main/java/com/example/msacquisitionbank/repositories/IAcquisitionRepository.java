package com.example.msacquisitionbank.repositories;

import com.example.msacquisitionbank.models.entities.Acquisition;
import reactor.core.publisher.Mono;

public interface IAcquisitionRepository extends IRepository<Acquisition, String>{
    Mono<Acquisition> findByCardNumber(String cardNumber);
}
