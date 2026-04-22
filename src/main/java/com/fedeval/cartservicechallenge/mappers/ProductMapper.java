package com.fedeval.cartservicechallenge.mappers;

import com.fedeval.cartservicechallenge.dtos.product.response.ProductResponse;
import com.fedeval.cartservicechallenge.models.Product;

public class ProductMapper {
    public static ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .code(product.getCode())
                .name(product.getName())
                .categoryName(product.getCategory().getName())
                .stock(product.getStock())
                .price(product.getPrice())
                .build();
    }
}
