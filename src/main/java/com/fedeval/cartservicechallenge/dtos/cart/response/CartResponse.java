package com.fedeval.cartservicechallenge.dtos.cart.response;

import lombok.Data;

import java.util.List;

@Data
public class CartResponse {
    private String code;
    private Long clientId;
    private String status;
    private List<CartItemResponse> items;
}
