package com.fedeval.cartservicechallenge.controllers;

import com.fedeval.cartservicechallenge.dtos.cart.request.CreateCartRequest;
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
@RequestMapping("/api/createCart")
public class CartController {

    private final CartService cartService;

    public CartController (CartService cartService){
        this.cartService = cartService;
    }

    @PostMapping
    public ResponseEntity<?> createCart(@RequestBody CreateCartRequest request){
        try {
            Cart cart = cartService.createCart(request.getClientId());

            return ResponseEntity.status(HttpStatus.CREATED).body(CartMapper.toResponse(cart));
        }catch (IllegalStateException e){
            return ResponseEntity.status(HttpStatus.CONFLICT).body (e.getMessage());
        }
    }
}
