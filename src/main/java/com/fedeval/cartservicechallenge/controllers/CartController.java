package com.fedeval.cartservicechallenge.controllers;

import com.fedeval.cartservicechallenge.dtos.cart.request.AddProductToCartRequest;
import com.fedeval.cartservicechallenge.dtos.cart.request.CreateCartRequest;
import com.fedeval.cartservicechallenge.dtos.cart.response.CartResponse;
import com.fedeval.cartservicechallenge.mappers.CartMapper;
import com.fedeval.cartservicechallenge.models.Cart;
import com.fedeval.cartservicechallenge.services.CartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CartController {

    private final CartService cartService;

    public CartController (CartService cartService){
        this.cartService = cartService;
    }

    @PostMapping ("/createCart")
    public ResponseEntity<?> createCart(@RequestBody CreateCartRequest request){
            Cart cart = cartService.createCart(request.getClientId());

            CartResponse response = CartMapper.toResponse(cart);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

    @PostMapping("/addProduct")
    public ResponseEntity<?> addProductToCart(@RequestBody AddProductToCartRequest request) {
            Cart cart = cartService.addProductToCart(
                    request.getCartCode(),
                    request.getProductCode(),
                    request.getQuantity()
            );

            CartResponse response = CartMapper.toResponse(cart);

            return ResponseEntity.ok(response);
    }
}
