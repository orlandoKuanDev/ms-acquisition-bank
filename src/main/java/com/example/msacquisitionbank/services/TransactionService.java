package com.example.msacquisitionbank.services;

import com.example.msacquisitionbank.models.dto.AverageBalanceDTO;
import com.example.msacquisitionbank.models.dto.AverageDTO;
import com.example.msacquisitionbank.models.entities.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Service
public class TransactionService {
    private final WebClient.Builder webClientBuilder;

    Logger logger = LoggerFactory.getLogger(TransactionService.class);

    @Autowired
    public TransactionService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    public Mono<AverageDTO> transactionAverage(String month, String accountNumber) {
        Map<String,String> params = new HashMap<>();
        params.put("month", month);
        params.put("accountNumber", accountNumber);
        return webClientBuilder
                .baseUrl("http://SERVICE-TRANSACTION/transaction")
                .build()
                .get()
                .uri("/average2/{month}/{accountNumber}", params)
                .accept(APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatus::isError, response -> {
                    logTraceResponse(logger, response);
                    return Mono.error(new RuntimeException(
                            String.format("THE ACCOUNT NUMBER DONT EXIST IN MICRO SERVICE TRANSACTION-> %s", accountNumber)
                    ));
                })
                .bodyToMono(AverageDTO.class);
    }

    public static void logTraceResponse(Logger log, ClientResponse response) {
        if (log.isTraceEnabled()) {
            log.trace("Response status: {}", response.statusCode());
            log.trace("Response headers: {}", response.headers().asHttpHeaders());
            response.bodyToMono(String.class)
                    .publishOn(Schedulers.boundedElastic())
                    .subscribe(body -> log.trace("Response body: {}", body));
        }
    }
}
