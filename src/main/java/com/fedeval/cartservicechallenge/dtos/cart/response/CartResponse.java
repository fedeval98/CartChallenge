package com.fedeval.cartservicechallenge.dtos.cart.response;

import lombok.Data;

@Data
public class CartResponse {
    private String code;
    private Long clientId;
    private String status;
}
