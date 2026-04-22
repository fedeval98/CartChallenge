package com.fedeval.cartservicechallenge.controllers;

import com.fedeval.cartservicechallenge.dtos.product.request.CreateProductRequest;
import com.fedeval.cartservicechallenge.dtos.product.request.UpdateProductRequest;
import com.fedeval.cartservicechallenge.dtos.product.response.ProductResponse;
import com.fedeval.cartservicechallenge.services.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/product")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/new")
    public ResponseEntity<ProductResponse> createProduct(
            @RequestBody @Valid CreateProductRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();

        ProductResponse response = productService.createProduct(request, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/patch/{productCode}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable @NotBlank String productCode,
            @RequestBody @Valid UpdateProductRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();

        ProductResponse response = productService.updateProduct(productCode, request, email);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping(Authentication authentication) {
        return ResponseEntity.ok("pong " + authentication.getName());
    }
}
