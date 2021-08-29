package com.example.msacquisitionbank.topic.consumer;

import com.example.msacquisitionbank.handler.AcquisitionHandler;
import com.example.msacquisitionbank.models.entities.Acquisition;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AcquisitionConsumer {
    private final static String SERVICE_WALLET_TOPIC = "service-wallet-topic";
    private final static String GROUP_ID = "acquisition-group";
    private final AcquisitionHandler acquisitionHandler;
    private final ObjectMapper objectMapper;

    @Autowired
    public AcquisitionConsumer(AcquisitionHandler acquisitionHandler, ObjectMapper objectMapper) {
        this.acquisitionHandler = acquisitionHandler;
        this.objectMapper = objectMapper;
    }

    @KafkaListener( topics = SERVICE_WALLET_TOPIC, groupId = GROUP_ID)
    public Disposable retrieveSavedAcquisition(String data) throws Exception {
        log.info("data from kafka listener (acquisition) =>"+data);
        Acquisition acquisition= objectMapper.readValue(data, Acquisition.class );
        return Mono.just(acquisition).as(acquisitionHandler::createAcquisition)
                .subscribe();
    }
}
