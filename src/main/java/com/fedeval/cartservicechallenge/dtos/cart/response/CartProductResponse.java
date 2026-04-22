package com.fedeval.cartservicechallenge.dtos.cart.response;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class CartProductResponse {
    Long productId;
    String productCode;
    String productName;
    BigDecimal price;
    Integer stock;
    Integer quantity;
    String categoryName;
    BigDecimal discountRate;
    BigDecimal finalPrice;
}
