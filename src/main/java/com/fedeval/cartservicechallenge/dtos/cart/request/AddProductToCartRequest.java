package com.fedeval.cartservicechallenge.dtos.cart.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class AddProductToCartRequest {

    @NotBlank(message = "Cart code is required")
    private String cartCode;

    @NotEmpty (message = "Products required")
    @Valid
    private List<CartProductItemRequest> products;
}
