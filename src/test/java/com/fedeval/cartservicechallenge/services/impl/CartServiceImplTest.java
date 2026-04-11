package com.fedeval.cartservicechallenge.services.impl;

import com.fedeval.cartservicechallenge.exceptions.BadRequestException;
import com.fedeval.cartservicechallenge.exceptions.ConflictException;
import com.fedeval.cartservicechallenge.exceptions.ResourceNotFoundException;
import com.fedeval.cartservicechallenge.models.Cart;
import com.fedeval.cartservicechallenge.models.CartItem;
import com.fedeval.cartservicechallenge.models.Client;
import com.fedeval.cartservicechallenge.models.Product;
import com.fedeval.cartservicechallenge.models.enums.CartStatus;
import com.fedeval.cartservicechallenge.repositories.CartItemRepository;
import com.fedeval.cartservicechallenge.repositories.CartRepository;
import com.fedeval.cartservicechallenge.repositories.ClientRepository;
import com.fedeval.cartservicechallenge.repositories.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartItemRepository cartItemRepository;

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
    void shouldThrowResourceNotFoundWhenClientDoesNotExist() {
        when(clientRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.createCart(1L)
        );

        assertEquals("Client not found", exception.getMessage());
    }

    @Test
    void shouldThrowConflictWhenClientAlreadyHasActiveCart() {
        Client client = new Client();
        client.setId(1L);

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(cartRepository.existsByClientIdAndStatus(1L, CartStatus.ACTIVE)).thenReturn(true);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> cartService.createCart(1L)
        );

        assertEquals("Client already has an active cart", exception.getMessage());
    }

    @Test
    void shouldAddNewProductToCartSuccessfully() {
        Cart cart = new Cart();
        cart.setCode("CART-123");
        cart.setStatus(CartStatus.ACTIVE);

        Product product = new Product();
        product.setCode("TECH-001");
        product.setStock(10);
        product.setPrice(BigDecimal.valueOf(100));

        when(cartRepository.findByCode("CART-123")).thenReturn(Optional.of(cart));
        when(productRepository.findByCode("TECH-001")).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartAndProduct(cart, product)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));

        Cart result = cartService.addProductToCart("CART-123", "TECH-001", 3);

        assertNotNull(result);
        assertEquals(cart, result);

        ArgumentCaptor<CartItem> captor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartItemRepository).save(captor.capture());

        CartItem savedItem = captor.getValue();
        assertEquals(cart, savedItem.getCart());
        assertEquals(product, savedItem.getProduct());
        assertEquals(3, savedItem.getQuantity());
    }

    @Test
    void shouldUpdateQuantityWhenProductAlreadyExistsInCart() {
        Cart cart = new Cart();
        cart.setCode("CART-123");
        cart.setStatus(CartStatus.ACTIVE);

        Product product = new Product();
        product.setCode("TECH-001");
        product.setStock(10);

        CartItem existingItem = new CartItem();
        existingItem.setCart(cart);
        existingItem.setProduct(product);
        existingItem.setQuantity(2);

        when(cartRepository.findByCode("CART-123")).thenReturn(Optional.of(cart));
        when(productRepository.findByCode("TECH-001")).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartAndProduct(cart, product)).thenReturn(Optional.of(existingItem));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));

        Cart result = cartService.addProductToCart("CART-123", "TECH-001", 3);

        assertNotNull(result);
        assertEquals(5, existingItem.getQuantity());
        verify(cartItemRepository, times(1)).save(existingItem);
    }

    @Test
    void shouldThrowBadRequestWhenQuantityIsNull() {
        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> cartService.addProductToCart("CART-123", "TECH-001", null)
        );

        assertEquals("Quantity must be greater than 0", ex.getMessage());
        verifyNoInteractions(cartRepository, productRepository, cartItemRepository);
    }

    @Test
    void shouldThrowBadRequestWhenQuantityIsZeroOrLess() {
        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> cartService.addProductToCart("CART-123", "TECH-001", 0)
        );

        assertEquals("Quantity must be greater than 0", ex.getMessage());
        verifyNoInteractions(cartRepository, productRepository, cartItemRepository);
    }

    @Test
    void shouldThrowResourceNotFoundWhenCartDoesNotExist() {
        when(cartRepository.findByCode("INVALID-CART")).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.addProductToCart("INVALID-CART", "TECH-001", 2)
        );

        assertEquals("Cart not found", ex.getMessage());
    }

    @Test
    void shouldThrowConflictWhenCartIsNotActive() {
        Cart cart = new Cart();
        cart.setCode("CART-123");
        cart.setStatus(CartStatus.COMPLETED);

        when(cartRepository.findByCode("CART-123")).thenReturn(Optional.of(cart));

        ConflictException ex = assertThrows(
                ConflictException.class,
                () -> cartService.addProductToCart("CART-123", "TECH-001", 2)
        );

        assertEquals("Cart is not active", ex.getMessage());
    }

    @Test
    void shouldThrowResourceNotFoundWhenProductDoesNotExist() {
        Cart cart = new Cart();
        cart.setCode("CART-123");
        cart.setStatus(CartStatus.ACTIVE);

        when(cartRepository.findByCode("CART-123")).thenReturn(Optional.of(cart));
        when(productRepository.findByCode("INVALID-PRODUCT")).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.addProductToCart("CART-123", "INVALID-PRODUCT", 2)
        );

        assertEquals("Product not found", ex.getMessage());
    }

    @Test
    void shouldThrowConflictWhenRequestedQuantityExceedsStockForNewItem() {
        Cart cart = new Cart();
        cart.setCode("CART-123");
        cart.setStatus(CartStatus.ACTIVE);

        Product product = new Product();
        product.setCode("TECH-001");
        product.setStock(5);

        when(cartRepository.findByCode("CART-123")).thenReturn(Optional.of(cart));
        when(productRepository.findByCode("TECH-001")).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartAndProduct(cart, product)).thenReturn(Optional.empty());

        ConflictException ex = assertThrows(
                ConflictException.class,
                () -> cartService.addProductToCart("CART-123", "TECH-001", 6)
        );

        assertEquals("Requested quantity exceeds available stock", ex.getMessage());
    }

    @Test
    void shouldThrowConflictWhenAccumulatedQuantityExceedsStock() {
        Cart cart = new Cart();
        cart.setCode("CART-123");
        cart.setStatus(CartStatus.ACTIVE);

        Product product = new Product();
        product.setCode("TECH-001");
        product.setStock(5);

        CartItem existingItem = new CartItem();
        existingItem.setCart(cart);
        existingItem.setProduct(product);
        existingItem.setQuantity(4);

        when(cartRepository.findByCode("CART-123")).thenReturn(Optional.of(cart));
        when(productRepository.findByCode("TECH-001")).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartAndProduct(cart, product)).thenReturn(Optional.of(existingItem));

        ConflictException ex = assertThrows(
                ConflictException.class,
                () -> cartService.addProductToCart("CART-123", "TECH-001", 2)
        );

        assertEquals("Requested quantity exceeds available stock", ex.getMessage());
    }
}
