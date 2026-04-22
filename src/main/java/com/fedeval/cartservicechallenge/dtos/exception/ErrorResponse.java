package com.fedeval.cartservicechallenge.dtos.exception;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class ErrorResponse {
        LocalDateTime timestamp;
        int status;
        String error;
        String message;
}
