package com.fedeval.cartservicechallenge.controllers;

import com.fedeval.cartservicechallenge.dtos.cart.request.AddProductToCartRequest;
import com.fedeval.cartservicechallenge.dtos.cart.request.CreateCartRequest;
import com.fedeval.cartservicechallenge.dtos.cart.response.CartDetailResponse;
import com.fedeval.cartservicechallenge.dtos.cart.response.CartProductResponse;
import com.fedeval.cartservicechallenge.dtos.cart.response.CartResponse;
import com.fedeval.cartservicechallenge.mappers.CartMapper;
import com.fedeval.cartservicechallenge.models.Cart;
import com.fedeval.cartservicechallenge.services.CartService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService){
        this.cartService = cartService;
    }

    @PostMapping("/createcart")
    public ResponseEntity<CartResponse> createCart(
            @RequestBody @Valid CreateCartRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();

        CartResponse response = cartService.createCart(request.getClientId(), email);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/addproduct")
    public ResponseEntity<CartResponse> addProductToCart(
            @RequestBody @Valid AddProductToCartRequest request,
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

    @DeleteMapping("/deleteproductfrom/{cartCode}/products/{productCode}")
    public ResponseEntity<CartResponse> removeProductFromCart(
            @PathVariable @NotBlank String cartCode,
            @PathVariable @NotBlank String productCode,
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

    @GetMapping("/{cartCode}/products")
    public ResponseEntity<CartDetailResponse> getCartProducts(
            @PathVariable @NotBlank String cartCode,
            Authentication authentication
    ) {
        String email = authentication.getName();
        CartDetailResponse response = cartService.getCartProducts(cartCode, email);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/processorder/{cartCode}")
    public ResponseEntity<String> processOrder(
            @PathVariable @NotBlank String cartCode,
            Authentication authentication
    ) {
        String email = authentication.getName();
        cartService.processCartOrder(cartCode, email);
        return ResponseEntity.accepted().body("Estamos procesando su orden");
    }

    @GetMapping("/mycarts")
    public ResponseEntity<List<CartResponse>> getMyCarts(Authentication authentication) {
        String email = authentication.getName();
        List<CartResponse> response = cartService.getCartsByClient(email);
        return ResponseEntity.ok(response);
    }
}
