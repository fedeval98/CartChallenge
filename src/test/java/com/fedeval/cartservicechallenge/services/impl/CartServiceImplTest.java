package com.fedeval.cartservicechallenge.services.impl;

import com.fedeval.cartservicechallenge.models.Cart;
import com.fedeval.cartservicechallenge.models.Client;
import com.fedeval.cartservicechallenge.models.enums.CartStatus;
import com.fedeval.cartservicechallenge.repositories.CartRepository;
import com.fedeval.cartservicechallenge.repositories.ClientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    @Test
    void shouldCreateCartSuccessfully() {
        Client client = new Client();
        client.setId(1L);

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(cartRepository.existsByClientIdAndStatus(1L, CartStatus.ACTIVE)).thenReturn(false);
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cart cart = cartService.createCart(1L);

        assertNotNull(cart);
        assertNotNull(cart.getCode());
        assertEquals(CartStatus.ACTIVE, cart.getStatus());
        assertEquals(client, cart.getClient());
    }

    @Test
    void shouldThrowExceptionWhenClientDoesNotExist() {
        when(clientRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> cartService.createCart(1L));

        assertEquals("Client not found", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenClientAlreadyHasActiveCart() {
        Client client = new Client();
        client.setId(1L);

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(cartRepository.existsByClientIdAndStatus(1L, CartStatus.ACTIVE)).thenReturn(true);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> cartService.createCart(1L)
        );

        assertEquals("Client already has an active cart", exception.getMessage());
    }

}
