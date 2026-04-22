package com.fedeval.cartservicechallenge.dtos.product.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateProductRequest {


    private String name;

    private Long categoryId;

    @Min(value = 0, message = "Stock must be 0 or greater")
    private Integer stock;

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

}
