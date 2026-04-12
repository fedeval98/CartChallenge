package com.fedeval.cartservicechallenge.mappers;

import com.fedeval.cartservicechallenge.dtos.cart.response.CartItemResponse;
import com.fedeval.cartservicechallenge.dtos.cart.response.CartResponse;
import com.fedeval.cartservicechallenge.models.Cart;

import java.util.stream.Collectors;

public class CartMapper {
    public static CartResponse toResponse(Cart cart) {
        CartResponse response = new CartResponse();
        response.setCode(cart.getCode());
        response.setClientId(cart.getClient().getId());
        response.setStatus(cart.getStatus().name());

        response.setItems(
                cart.getItems().stream()
                        .map(item -> {
                            CartItemResponse itemResponse = new CartItemResponse();
                            itemResponse.setProductCode(item.getProduct().getCode());
                            itemResponse.setQuantity(item.getQuantity());
                            return itemResponse;
                        })
                        .collect(Collectors.toList())
        );

        return response;
    }
}
