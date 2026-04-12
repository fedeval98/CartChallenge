package com.fedeval.cartservicechallenge.dtos.cart.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemResponse {

    private String productCode;
    private Integer quantity;
}
