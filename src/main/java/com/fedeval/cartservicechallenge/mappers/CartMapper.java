package com.fedeval.cartservicechallenge.mappers;

import com.fedeval.cartservicechallenge.dtos.cart.response.CartResponse;
import com.fedeval.cartservicechallenge.models.Cart;

public class CartMapper {
    public static CartResponse toResponse(Cart cart) {
        CartResponse response = new CartResponse();
        response.setCode(cart.getCode());
        response.setClientId(cart.getClient().getId());
        response.setStatus(cart.getStatus().name());
        return response;
    }
}
