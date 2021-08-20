package com.example.msacquisitionbank.models.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Payment {
    @Field(name = "amount")
    private Double amount;

    @Field(name = "acquisition")
    private Acquisition acquisition;

    @Field(name = "description")
    private String description;

    @Field(name = "creditLine")
    private Double creditLine;

    @Field(name = "creditLine")
    private Boolean haveDebt;

    @Field(name = "paymentDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expirationDate;
}
