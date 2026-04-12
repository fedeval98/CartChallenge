package com.fedeval.cartservicechallenge.services.impl;

import com.fedeval.cartservicechallenge.dtos.cart.response.CartResponse;
import com.fedeval.cartservicechallenge.exceptions.BadRequestException;
import com.fedeval.cartservicechallenge.exceptions.ConflictException;
import com.fedeval.cartservicechallenge.exceptions.ForbiddenException;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
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

    private static final String EMAIL = "test@mail.com";
    private static final String OTHER_EMAIL = "other@mail.com";

    private Cart buildCart() {
        Client client = new Client();
        client.setId(1L);
        client.setEmail(EMAIL);

        Cart cart = new Cart();
        cart.setCode("CART-123");
        cart.setStatus(CartStatus.ACTIVE);
        cart.setClient(client);
        cart.setItems(new ArrayList<>());

        return cart;
    }

    @Test
    void shouldCreateCartSuccessfully() {
        Client client = new Client();
        client.setId(1L);
        client.setEmail(EMAIL);

        when(clientRepository.findByEmail(EMAIL)).thenReturn(Optional.of(client));
        when(cartRepository.existsByClientIdAndStatus(1L, CartStatus.ACTIVE)).thenReturn(false);
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartResponse cart = cartService.createCart(1L, EMAIL);

        assertNotNull(cart);
        assertNotNull(cart.getCode());
        assertEquals("ACTIVE", cart.getStatus());
        assertEquals(1L, cart.getClientId());
    }

    @Test
    void shouldThrowResourceNotFoundWhenAuthenticatedClientDoesNotExist() {
        when(clientRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.createCart(1L, EMAIL)
        );

        assertEquals("Authenticated client not found", exception.getMessage());
    }

    @Test
    void shouldThrowConflictWhenClientAlreadyHasActiveCart() {
        Client client = new Client();
        client.setId(1L);
        client.setEmail(EMAIL);

        when(clientRepository.findByEmail(EMAIL)).thenReturn(Optional.of(client));
        when(cartRepository.existsByClientIdAndStatus(1L, CartStatus.ACTIVE)).thenReturn(true);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> cartService.createCart(1L, EMAIL)
        );

        assertEquals("Client already has an active cart", exception.getMessage());
    }

    @Test
    void shouldAddNewProductToCartSuccessfully() {
        Cart cart = buildCart();

        Product product = new Product();
        product.setCode("TECH-001");
        product.setStock(10);

        when(cartRepository.findByCode("CART-123")).thenReturn(Optional.of(cart));
        when(productRepository.findByCode("TECH-001")).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartAndProduct(cart, product)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));

        CartResponse result = cartService.addProductToCart("CART-123", "TECH-001", 3, EMAIL);

        assertNotNull(result);
        assertEquals("CART-123", result.getCode());
        assertEquals("ACTIVE", result.getStatus());
        assertEquals(1L, result.getClientId());

        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void shouldUpdateQuantityWhenProductAlreadyExistsInCart() {
        Cart cart = buildCart();

        Product product = new Product();
        product.setCode("TECH-001");
        product.setStock(10);

        CartItem existingItem = new CartItem();
        existingItem.setCart(cart);
        existingItem.setProduct(product);
        existingItem.setQuantity(2);

        cart.getItems().add(existingItem);

        when(cartRepository.findByCode("CART-123")).thenReturn(Optional.of(cart));
        when(productRepository.findByCode("TECH-001")).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartAndProduct(cart, product)).thenReturn(Optional.of(existingItem));

        CartResponse result = cartService.addProductToCart("CART-123", "TECH-001", 3, EMAIL);

        assertNotNull(result);
        assertEquals(5, existingItem.getQuantity());
        assertEquals(1L, result.getClientId());

        verify(cartItemRepository).save(existingItem);
    }

    @Test
    void shouldThrowBadRequestWhenQuantityIsNull() {
        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> cartService.addProductToCart("CART-123", "TECH-001", null, EMAIL)
        );

        assertEquals("Quantity must be greater than 0", ex.getMessage());
        verifyNoInteractions(cartRepository, productRepository, cartItemRepository);
    }

    @Test
    void shouldThrowBadRequestWhenQuantityIsZeroOrLess() {
        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> cartService.addProductToCart("CART-123", "TECH-001", 0, EMAIL)
        );

        assertEquals("Quantity must be greater than 0", ex.getMessage());
        verifyNoInteractions(cartRepository, productRepository, cartItemRepository);
    }

    @Test
    void shouldThrowResourceNotFoundWhenCartDoesNotExist() {
        when(cartRepository.findByCode("INVALID-CART")).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.addProductToCart("INVALID-CART", "TECH-001", 2, EMAIL)
        );

        assertEquals("Cart not found", ex.getMessage());
    }

    @Test
    void shouldThrowForbiddenWhenCartDoesNotBelongToUser() {
        Cart cart = buildCart();

        when(cartRepository.findByCode("CART-123")).thenReturn(Optional.of(cart));

        ForbiddenException ex = assertThrows(
                ForbiddenException.class,
                () -> cartService.addProductToCart("CART-123", "TECH-001", 2, OTHER_EMAIL)
        );

        assertEquals("You cannot access this cart", ex.getMessage());
        verifyNoInteractions(productRepository, cartItemRepository);
    }

    @Test
    void shouldThrowConflictWhenCartIsNotActive() {
        Cart cart = buildCart();
        cart.setStatus(CartStatus.COMPLETED);

        when(cartRepository.findByCode("CART-123")).thenReturn(Optional.of(cart));

        ConflictException ex = assertThrows(
                ConflictException.class,
                () -> cartService.addProductToCart("CART-123", "TECH-001", 2, EMAIL)
        );

        assertEquals("Cart is not active", ex.getMessage());
    }

    @Test
    void shouldThrowResourceNotFoundWhenProductDoesNotExist() {
        Cart cart = buildCart();

        when(cartRepository.findByCode("CART-123")).thenReturn(Optional.of(cart));
        when(productRepository.findByCode("INVALID-PRODUCT")).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.addProductToCart("CART-123", "INVALID-PRODUCT", 2, EMAIL)
        );

        assertEquals("Product not found", ex.getMessage());
    }

    @Test
    void shouldThrowConflictWhenRequestedQuantityExceedsStockForNewItem() {
        Cart cart = buildCart();

        Product product = new Product();
        product.setCode("TECH-001");
        product.setStock(5);

        when(cartRepository.findByCode("CART-123")).thenReturn(Optional.of(cart));
        when(productRepository.findByCode("TECH-001")).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartAndProduct(cart, product)).thenReturn(Optional.empty());

        ConflictException ex = assertThrows(
                ConflictException.class,
                () -> cartService.addProductToCart("CART-123", "TECH-001", 6, EMAIL)
        );

        assertEquals("Requested quantity exceeds available stock", ex.getMessage());
    }

    @Test
    void shouldThrowConflictWhenAccumulatedQuantityExceedsStock() {
        Cart cart = buildCart();

        Product product = new Product();
        product.setCode("TECH-001");
        product.setStock(5);

        CartItem existingItem = new CartItem();
        existingItem.setCart(cart);
        existingItem.setProduct(product);
        existingItem.setQuantity(4);

        cart.getItems().add(existingItem);

        when(cartRepository.findByCode("CART-123")).thenReturn(Optional.of(cart));
        when(productRepository.findByCode("TECH-001")).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartAndProduct(cart, product)).thenReturn(Optional.of(existingItem));

        ConflictException ex = assertThrows(
                ConflictException.class,
                () -> cartService.addProductToCart("CART-123", "TECH-001", 2, EMAIL)
        );

        assertEquals("Requested quantity exceeds available stock", ex.getMessage());
    }

    @Test
    void shouldRemoveProductFromCartSuccessfully() {
        Cart cart = buildCart();

        Product product = new Product();
        product.setCode("TECH-003");

        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(10);

        cart.getItems().add(cartItem);

        when(cartRepository.findByCode("CART-123")).thenReturn(Optional.of(cart));
        when(productRepository.findByCode("TECH-003")).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartAndProduct(cart, product)).thenReturn(Optional.of(cartItem));

        CartResponse result = cartService.removeProductFromCart("CART-123", "TECH-003", EMAIL);

        assertNotNull(result);
        assertEquals("CART-123", result.getCode());
        assertEquals("ACTIVE", result.getStatus());
        assertEquals(1L, result.getClientId());
        assertTrue(result.getItems().isEmpty());

        verify(cartItemRepository).delete(cartItem);
    }

    @Test
    void shouldThrowForbiddenWhenRemovingProductFromAnotherUsersCart() {
        Cart cart = buildCart();

        when(cartRepository.findByCode("CART-123")).thenReturn(Optional.of(cart));

        ForbiddenException ex = assertThrows(
                ForbiddenException.class,
                () -> cartService.removeProductFromCart("CART-123", "TECH-003", OTHER_EMAIL)
        );

        assertEquals("You cannot access this cart", ex.getMessage());
        verifyNoInteractions(productRepository, cartItemRepository);
    }

    @Test
    void shouldThrowConflictWhenRemovingProductFromInactiveCart() {
        Cart cart = buildCart();
        cart.setStatus(CartStatus.COMPLETED);

        when(cartRepository.findByCode("CART-123")).thenReturn(Optional.of(cart));

        ConflictException ex = assertThrows(
                ConflictException.class,
                () -> cartService.removeProductFromCart("CART-123", "TECH-003", EMAIL)
        );

        assertEquals("Cart is not active", ex.getMessage());
    }

    @Test
    void shouldThrowResourceNotFoundWhenRemovingProductThatDoesNotExist() {
        Cart cart = buildCart();

        when(cartRepository.findByCode("CART-123")).thenReturn(Optional.of(cart));
        when(productRepository.findByCode("INVALID-PRODUCT")).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.removeProductFromCart("CART-123", "INVALID-PRODUCT", EMAIL)
        );

        assertEquals("Product not found", ex.getMessage());
    }

    @Test
    void shouldThrowResourceNotFoundWhenProductIsNotInCart() {
        Cart cart = buildCart();

        Product product = new Product();
        product.setCode("TECH-003");

        when(cartRepository.findByCode("CART-123")).thenReturn(Optional.of(cart));
        when(productRepository.findByCode("TECH-003")).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartAndProduct(cart, product)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.removeProductFromCart("CART-123", "TECH-003", EMAIL)
        );

        assertEquals("Product is not in cart", ex.getMessage());
    }
}