package com.fedeval.cartservicechallenge.dtos.cart.response;

import lombok.Data;

import java.util.List;

@Data
public class CartDetailResponse {

    private Long clientId;
    private String cartCode;
    private String status;
    private List<CartProductResponse> products;
}
