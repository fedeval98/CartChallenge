package com.fedeval.cartservicechallenge.dtos.product.response;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class ProductResponse {
    Long id;
    String code;
    String name;
    String categoryName;
    Integer stock;
    BigDecimal price;
}