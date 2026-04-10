package com.fedeval.cartservicechallenge.dtos.cart.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateCartRequest {
    @NotNull (message = "Cliend ID is required")
    private Long clientId;
}
