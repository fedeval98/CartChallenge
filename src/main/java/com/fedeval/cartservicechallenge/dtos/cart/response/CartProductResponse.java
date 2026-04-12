package com.fedeval.cartservicechallenge.dtos.cart.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartProductResponse {
    private Long productId;
    private String productCode;
    private String productName;
    private BigDecimal price;
    private Integer stock;
    private Integer quantity;
    private String categoryName;
    private BigDecimal discountRate;
    private BigDecimal finalPrice;
}
