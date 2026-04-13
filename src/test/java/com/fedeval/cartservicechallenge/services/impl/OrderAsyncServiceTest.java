package com.fedeval.cartservicechallenge.services.impl;

import com.fedeval.cartservicechallenge.exceptions.BadRequestException;
import com.fedeval.cartservicechallenge.exceptions.ConflictException;
import com.fedeval.cartservicechallenge.exceptions.ForbiddenException;
import com.fedeval.cartservicechallenge.models.*;
import com.fedeval.cartservicechallenge.models.enums.CartStatus;
import com.fedeval.cartservicechallenge.repositories.CartRepository;
import com.fedeval.cartservicechallenge.repositories.OrderItemRepository;
import com.fedeval.cartservicechallenge.repositories.OrderRepository;
import com.fedeval.cartservicechallenge.repositories.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderAsyncServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderAsyncService orderAsyncService;

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
    void shouldThrowForbiddenWhenCartDoesNotBelongToUser() {
        Cart cart = buildCart();

        when(cartRepository.findByCode("CART-123")).thenReturn(Optional.of(cart));

        ForbiddenException ex = assertThrows(
                ForbiddenException.class,
                () -> orderAsyncService.processOrderAsync("CART-123", OTHER_EMAIL)
        );

        assertEquals("You cannot access this cart", ex.getMessage());
    }

    @Test
    void shouldThrowConflictWhenCartIsNotActive() {
        Cart cart = buildCart();
        cart.setStatus(CartStatus.COMPLETED);

        when(cartRepository.findByCode("CART-123")).thenReturn(Optional.of(cart));

        ConflictException ex = assertThrows(
                ConflictException.class,
                () -> orderAsyncService.processOrderAsync("CART-123", EMAIL)
        );

        assertEquals("Cart is not active", ex.getMessage());
    }

    @Test
    void shouldThrowBadRequestWhenCartIsEmpty() {
        Cart cart = buildCart();

        when(cartRepository.findByCode("CART-123")).thenReturn(Optional.of(cart));

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> orderAsyncService.processOrderAsync("CART-123", EMAIL)
        );

        assertEquals("Cart is empty", ex.getMessage());
    }

    @Test
    void shouldProcessOrderSuccessfully() {
        Cart cart = buildCart();

        Category category = new Category();
        category.setName("Laptops");
        category.setDiscountRate(new BigDecimal("10"));

        Product product = new Product();
        product.setCode("PROD-001");
        product.setName("Notebook");
        product.setPrice(new BigDecimal("1000"));
        product.setStock(10);
        product.setCategory(category);

        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(2);

        cart.getItems().add(item);

        when(cartRepository.findByCode("CART-123")).thenReturn(Optional.of(cart));
        when(orderRepository.save(any(CustomerOrder.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderItemRepository.save(any(CustomerOrderItem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> orderAsyncService.processOrderAsync("CART-123", EMAIL));

        assertEquals(CartStatus.COMPLETED, cart.getStatus());
        assertEquals(8, product.getStock());

        verify(orderRepository, atLeastOnce()).save(any(CustomerOrder.class));
        verify(orderItemRepository).save(any(CustomerOrderItem.class));
        verify(productRepository).save(product);
        verify(cartRepository, atLeastOnce()).save(cart);
    }

    @Test
    void shouldRestoreCartToActiveWhenStockIsInsufficient() {
        Cart cart = buildCart();

        Category category = new Category();
        category.setDiscountRate(BigDecimal.ZERO);

        Product product = new Product();
        product.setCode("PROD-001");
        product.setPrice(new BigDecimal("1000"));
        product.setStock(1);
        product.setCategory(category);

        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(2);

        cart.getItems().add(item);

        when(cartRepository.findByCode("CART-123")).thenReturn(Optional.of(cart));
        when(orderRepository.save(any(CustomerOrder.class))).thenAnswer(inv -> inv.getArgument(0));
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        ConflictException ex = assertThrows(
                ConflictException.class,
                () -> orderAsyncService.processOrderAsync("CART-123", EMAIL)
        );

        assertTrue(ex.getMessage().contains("Insufficient stock for product"));
        assertEquals(CartStatus.ACTIVE, cart.getStatus());
    }
}
