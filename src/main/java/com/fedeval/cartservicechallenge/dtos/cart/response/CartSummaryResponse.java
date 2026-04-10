package com.fedeval.cartservicechallenge.dtos.cart.response;

import lombok.Data;

@Data
public class CartSummaryResponse {

    private String code;
    private String status;
    private Integer totalItems;
}
