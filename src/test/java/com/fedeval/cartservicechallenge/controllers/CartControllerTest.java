package com.fedeval.cartservicechallenge.controllers;

import com.fedeval.cartservicechallenge.exceptions.ResourceNotFoundException;
import com.fedeval.cartservicechallenge.services.CartService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    private static final String EMAIL = "test@mail.com";

    @Mock
    private CartService cartService;

    @InjectMocks
    private CartController cartController;

    @Test
    void shouldProcessCartOrderAndReturnAcceptedMessage() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(EMAIL);

        ResponseEntity<?> response = cartController.processOrder("CART-123", authentication);

        assertNotNull(response);
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertEquals("Estamos procesando su orden", response.getBody());

        verify(authentication).getName();
        verify(cartService).processCartOrder("CART-123", EMAIL);
    }

    @Test
    void shouldThrowExceptionWhenCartDoesNotExistWhileProcessingOrder() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(EMAIL);

        doThrow(new ResourceNotFoundException("Cart not found"))
                .when(cartService).processCartOrder("INVALID-CART", EMAIL);

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> cartController.processOrder("INVALID-CART", authentication)
        );

        assertEquals("Cart not found", exception.getMessage());

        verify(authentication).getName();
        verify(cartService).processCartOrder("INVALID-CART", EMAIL);
    }
}
