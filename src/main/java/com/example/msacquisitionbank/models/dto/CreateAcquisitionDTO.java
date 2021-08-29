package com.example.msacquisitionbank.models.dto;

import com.example.msacquisitionbank.models.entities.Customer;
import com.example.msacquisitionbank.models.entities.Product;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateAcquisitionDTO {
    private Product product;
    private Customer customerHolder;
    private Double initial;
}