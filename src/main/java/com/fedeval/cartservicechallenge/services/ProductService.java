package com.fedeval.cartservicechallenge.services;

import com.fedeval.cartservicechallenge.dtos.product.request.CreateProductRequest;
import com.fedeval.cartservicechallenge.dtos.product.request.UpdateProductRequest;
import com.fedeval.cartservicechallenge.dtos.product.response.ProductResponse;

public interface ProductService {
    ProductResponse createProduct (CreateProductRequest request, String email);
    ProductResponse updateProduct(String productCode, UpdateProductRequest request, String email);
}
