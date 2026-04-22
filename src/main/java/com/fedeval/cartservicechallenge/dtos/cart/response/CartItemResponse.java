package com.fedeval.cartservicechallenge.dtos.cart.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CartItemResponse {

    String productCode;
    Integer quantity;
}
