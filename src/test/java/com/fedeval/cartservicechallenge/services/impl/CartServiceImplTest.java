package com.fedeval.cartservicechallenge.services.impl;

import com.fedeval.cartservicechallenge.dtos.cart.response.CartDetailResponse;
import com.fedeval.cartservicechallenge.dtos.cart.response.CartProductResponse;
import com.fedeval.cartservicechallenge.dtos.cart.response.CartResponse;
import com.fedeval.cartservicechallenge.exceptions.BadRequestException;
import com.fedeval.cartservicechallenge.exceptions.ConflictException;
import com.fedeval.cartservicechallenge.exceptions.ForbiddenException;
import com.fedeval.cartservicechallenge.exceptions.ResourceNotFoundException;
import com.fedeval.cartservicechallenge.models.*;
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


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
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

    @Mock
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

    @Test
    void shouldReturnCartProductsSuccessfully() {
        Cart cart = buildCart();

        Category category = new Category();
        category.setName("Laptops");
        category.setDiscountRate(new BigDecimal("10.00"));

        Product product = new Product();
        product.setId(1L);
        product.setCode("TECH-001");
        product.setName("Notebook Gamer");
        product.setPrice(new BigDecimal("1000.00"));
        product.setStock(5);
        product.setCategory(category);

        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);

        cart.setItems(new ArrayList<>());
        cart.getItems().add(cartItem);

        when(cartRepository.findByCode("CART-123")).thenReturn(Optional.of(cart));

        CartDetailResponse result = cartService.getCartProducts("CART-123", EMAIL);

        assertNotNull(result);
        assertEquals(1L, result.getClientId());
        assertEquals("CART-123", result.getCartCode());
        assertEquals("ACTIVE", result.getStatus());

        assertNotNull(result.getProducts());
        assertEquals(1, result.getProducts().size());

        CartProductResponse response = result.getProducts().get(0);
        assertEquals(1L, response.getProductId());
        assertEquals("TECH-001", response.getProductCode());
        assertEquals("Notebook Gamer", response.getProductName());
        assertEquals(new BigDecimal("1000.00"), response.getPrice());
        assertEquals(5, response.getStock());
        assertEquals(2, response.getQuantity());
        assertEquals("Laptops", response.getCategoryName());
        assertEquals(new BigDecimal("10.00"), response.getDiscountRate());
        assertEquals(new BigDecimal("900.0000"), response.getFinalPrice());

        verify(cartRepository).findByCode("CART-123");
    }

    @Test
    void shouldThrowResourceNotFoundWhenCartDoesNotExistForGetCartProducts() {
        when(cartRepository.findByCode("INVALID-CART")).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.getCartProducts("INVALID-CART", EMAIL)
        );

        assertEquals("Cart not found", exception.getMessage());
    }

    @Test
    void shouldThrowForbiddenWhenGettingProductsFromAnotherUsersCart() {
        Cart cart = buildCart();

        when(cartRepository.findByCode("CART-123")).thenReturn(Optional.of(cart));

        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> cartService.getCartProducts("CART-123", OTHER_EMAIL)
        );

        assertEquals("You cannot access this cart", exception.getMessage());
    }

    @Test
    void shouldReturnEmptyProductListWhenCartHasNoProducts() {
        Cart cart = buildCart();
        cart.setItems(new ArrayList<>());

        when(cartRepository.findByCode("CART-123")).thenReturn(Optional.of(cart));

        CartDetailResponse result = cartService.getCartProducts("CART-123", EMAIL);

        assertNotNull(result);
        assertEquals(1L, result.getClientId());
        assertEquals("CART-123", result.getCartCode());
        assertEquals("ACTIVE", result.getStatus());
        assertNotNull(result.getProducts());
        assertTrue(result.getProducts().isEmpty());
    }

    @Test
    void shouldTriggerAsyncOrderProcessing() {
        Client client = Client.builder()
                .email(EMAIL)
                .build();

        Category category = Category.builder()
                .name("General")
                .discountRate(BigDecimal.ZERO)
                .build();

        Product product = Product.builder()
                .code("PROD-1")
                .name("Producto")
                .stock(10)
                .price(new BigDecimal("1000"))
                .category(category)
                .build();

        CartItem item = CartItem.builder()
                .product(product)
                .quantity(1)
                .build();

        Cart cart = Cart.builder()
                .code("CART-123")
                .client(client)
                .items(List.of(item))
                .status(CartStatus.ACTIVE)
                .build();

        when(cartRepository.findByCode("CART-123")).thenReturn(Optional.of(cart));

        cartService.processCartOrder("CART-123", EMAIL);

        verify(orderAsyncService).processOrderAsync("CART-123", EMAIL);
    }

    @Test
    void shouldReturnAllCartsForAuthenticatedClient() {
        Client client = new Client();
        client.setId(1L);
        client.setEmail(EMAIL);

        Cart cart1 = new Cart();
        cart1.setCode("CART-123");
        cart1.setStatus(CartStatus.ACTIVE);
        cart1.setClient(client);
        cart1.setItems(new ArrayList<>());

        Cart cart2 = new Cart();
        cart2.setCode("CART-456");
        cart2.setStatus(CartStatus.COMPLETED);
        cart2.setClient(client);
        cart2.setItems(new ArrayList<>());

        when(clientRepository.findByEmail(EMAIL)).thenReturn(Optional.of(client));
        when(cartRepository.findAllByClientId(1L)).thenReturn(List.of(cart1, cart2));

        List<CartResponse> result = cartService.getCartsByClient(EMAIL);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("CART-123", result.get(0).getCode());
        assertEquals("ACTIVE", result.get(0).getStatus());
        assertEquals("CART-456", result.get(1).getCode());
        assertEquals("COMPLETED", result.get(1).getStatus());
    }
}