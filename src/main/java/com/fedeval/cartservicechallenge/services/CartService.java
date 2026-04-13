package com.fedeval.cartservicechallenge.services;

import com.fedeval.cartservicechallenge.dtos.cart.response.CartDetailResponse;
import com.fedeval.cartservicechallenge.dtos.cart.response.CartResponse;

import java.util.List;

public interface CartService {
    CartResponse createCart(Long clientid, String email);

    CartResponse addProductToCart(String cartCode, String productCode, Integer quantity, String email);

    CartResponse removeProductFromCart(String cartCode, String productCode, String email);

    CartDetailResponse getCartProducts(String cartCode, String email);

    void processCartOrder(String cartCode, String email);

    List<CartResponse> getCartsByClient(String email);
}
