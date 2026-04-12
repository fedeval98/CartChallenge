package com.fedeval.cartservicechallenge.services;

import com.fedeval.cartservicechallenge.dtos.cart.response.CartResponse;

public interface CartService {
    CartResponse createCart(Long clientid, String email);

    CartResponse addProductToCart(String cartCode, String productCode, Integer quantity, String email);

    CartResponse removeProductFromCart(String cartCode, String productCode, String email);
}
