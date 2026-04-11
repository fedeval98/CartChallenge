package com.fedeval.cartservicechallenge.dtos.product.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductResponse {

    private String code;
    private String name;
    private String category;
    private BigDecimal price;
    private Integer stock;

}
