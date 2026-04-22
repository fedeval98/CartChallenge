package com.fedeval.cartservicechallenge.dtos.product.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProductResponse {
    private Long id;
    private String code;
    private String name;
    private String categoryName;
    private Integer stock;
    private BigDecimal price;
}