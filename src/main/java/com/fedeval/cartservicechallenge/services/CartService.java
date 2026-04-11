package com.fedeval.cartservicechallenge.services;

import com.fedeval.cartservicechallenge.models.Cart;

public interface CartService {
    Cart createCart (Long clientId);
    Cart addProductToCart(String cartcode, String productCode, Integer quantity);
}
