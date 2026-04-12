package com.fedeval.cartservicechallenge.controllers;

import com.fedeval.cartservicechallenge.dtos.cart.request.AddProductToCartRequest;
import com.fedeval.cartservicechallenge.dtos.cart.request.CreateCartRequest;
import com.fedeval.cartservicechallenge.dtos.cart.response.CartResponse;
import com.fedeval.cartservicechallenge.mappers.CartMapper;
import com.fedeval.cartservicechallenge.models.Cart;
import com.fedeval.cartservicechallenge.services.CartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService){
        this.cartService = cartService;
    }

    @PostMapping("/cart/createCart")
    public ResponseEntity<?> createCart(
            @RequestBody CreateCartRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();

        CartResponse response = cartService.createCart(request.getClientId(), email);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/cart/addProduct")
    public ResponseEntity<?> addProductToCart(
            @RequestBody AddProductToCartRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();

        CartResponse response = cartService.addProductToCart(
                request.getCartCode(),
                request.getProductCode(),
                request.getQuantity(),
                email
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/cart/deleteProductFrom/{cartCode}/products/{productCode}")
    public ResponseEntity<?> removeProductFromCart(
            @PathVariable String cartCode,
            @PathVariable String productCode,
            Authentication authentication
    ) {
        String email = authentication.getName();

        CartResponse response = cartService.removeProductFromCart(
                cartCode,
                productCode,
                email
        );

        return ResponseEntity.ok(response);
    }
}
