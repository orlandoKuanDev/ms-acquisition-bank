package com.example.msacquisitionbank.models.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class AverageDTO {
    private List<Double> balances;
    private Double average;
    private String productName;
    private String customerIdentityType;
}