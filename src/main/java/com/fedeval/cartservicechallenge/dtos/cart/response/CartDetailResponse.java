package com.fedeval.cartservicechallenge.dtos.cart.response;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class CartDetailResponse {

    Long clientId;
    String cartCode;
    String status;
    List<CartProductResponse> products;
}
