package com.fedeval.cartservicechallenge.mappers;

import com.fedeval.cartservicechallenge.dtos.cart.response.CartItemResponse;
import com.fedeval.cartservicechallenge.dtos.cart.response.CartProductResponse;
import com.fedeval.cartservicechallenge.dtos.cart.response.CartResponse;
import com.fedeval.cartservicechallenge.models.Cart;
import com.fedeval.cartservicechallenge.models.CartItem;
import com.fedeval.cartservicechallenge.models.Product;

import java.math.BigDecimal;

public class CartMapper {
    public static CartResponse toResponse(Cart cart) {
        return CartResponse.builder()
                .code(cart.getCode())
                .clientId(cart.getClient().getId())
                .status(cart.getStatus().name())
                .items(cart.getItems().stream()
                        .map(item -> CartItemResponse.builder()
                                .productCode(item.getProduct().getCode())
                                .quantity(item.getQuantity())
                                .build()
                        )
                        .toList()
                )
                .build();
    }

    public static CartProductResponse toCartProductResponse(CartItem cartItem) {
        Product product = cartItem.getProduct();

        String categoryName = null;
        BigDecimal discountRate = BigDecimal.ZERO;
        BigDecimal finalPrice = product.getPrice();

        if (product.getCategory() != null) {
            categoryName = product.getCategory().getName();

            if (product.getCategory().getDiscountRate() != null) {
                discountRate = product.getCategory().getDiscountRate();
            }

            finalPrice = product.getPrice()
                    .subtract(product.getPrice().multiply(discountRate).divide(BigDecimal.valueOf(100)));
        }
        return CartProductResponse.builder()
                .productId(product.getId())
                .productCode(product.getCode())
                .productName(product.getName())
                .price(product.getPrice())
                .stock(product.getStock())
                .quantity(cartItem.getQuantity())
                .categoryName(categoryName)
                .discountRate(discountRate)
                .finalPrice(finalPrice)
                .build();
    }
}
