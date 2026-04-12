package com.fedeval.cartservicechallenge.mappers;

import com.fedeval.cartservicechallenge.dtos.cart.response.CartItemResponse;
import com.fedeval.cartservicechallenge.dtos.cart.response.CartProductResponse;
import com.fedeval.cartservicechallenge.dtos.cart.response.CartResponse;
import com.fedeval.cartservicechallenge.models.Cart;
import com.fedeval.cartservicechallenge.models.CartItem;
import com.fedeval.cartservicechallenge.models.Product;

import java.math.BigDecimal;
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

    public static CartProductResponse toCartProductResponse(CartItem cartItem) {
        Product product = cartItem.getProduct();

        CartProductResponse response = new CartProductResponse();
        response.setProductId(product.getId());
        response.setProductCode(product.getCode());
        response.setProductName(product.getName());
        response.setPrice(product.getPrice());
        response.setStock(product.getStock());
        response.setQuantity(cartItem.getQuantity());

        if (product.getCategory() != null) {
            response.setCategoryName(product.getCategory().getName());
            response.setDiscountRate(product.getCategory().getDiscountRate());

            BigDecimal discount = product.getCategory().getDiscountRate();
            BigDecimal finalPrice = product.getPrice()
                    .subtract(product.getPrice().multiply(discount).divide(BigDecimal.valueOf(100)));

            response.setFinalPrice(finalPrice);
        } else {
            response.setFinalPrice(product.getPrice());
        }

        return response;
    }
}
