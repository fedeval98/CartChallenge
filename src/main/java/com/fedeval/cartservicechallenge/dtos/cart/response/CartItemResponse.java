package com.fedeval.cartservicechallenge.dtos.cart.response;

import lombok.Data;

@Data
public class CartItemResponse {

    private String productCode;
    private Integer quantity;
}
