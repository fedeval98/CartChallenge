package com.fedeval.cartservicechallenge.dtos.cart.response;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class CartResponse {
    String code;
    Long clientId;
    String status;
    List<CartItemResponse> items;
}
