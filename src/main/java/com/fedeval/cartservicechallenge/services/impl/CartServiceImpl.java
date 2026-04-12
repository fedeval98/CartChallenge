package com.fedeval.cartservicechallenge.services.impl;

import com.fedeval.cartservicechallenge.dtos.cart.response.CartDetailResponse;
import com.fedeval.cartservicechallenge.dtos.cart.response.CartProductResponse;
import com.fedeval.cartservicechallenge.dtos.cart.response.CartResponse;
import com.fedeval.cartservicechallenge.exceptions.BadRequestException;
import com.fedeval.cartservicechallenge.exceptions.ConflictException;
import com.fedeval.cartservicechallenge.exceptions.ForbiddenException;
import com.fedeval.cartservicechallenge.exceptions.ResourceNotFoundException;
import com.fedeval.cartservicechallenge.mappers.CartMapper;
import com.fedeval.cartservicechallenge.models.Cart;
import com.fedeval.cartservicechallenge.models.CartItem;
import com.fedeval.cartservicechallenge.models.Client;
import com.fedeval.cartservicechallenge.models.Product;
import com.fedeval.cartservicechallenge.models.enums.CartStatus;
import com.fedeval.cartservicechallenge.repositories.CartItemRepository;
import com.fedeval.cartservicechallenge.repositories.CartRepository;
import com.fedeval.cartservicechallenge.repositories.ClientRepository;
import com.fedeval.cartservicechallenge.repositories.ProductRepository;
import com.fedeval.cartservicechallenge.services.CartService;
import com.fedeval.cartservicechallenge.utils.CodeGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderAsyncService orderAsyncService;

    public CartServiceImpl(
            CartRepository cartRepository,
            ClientRepository clientRepository,
            ProductRepository productRepository,
            CartItemRepository cartItemRepository,
            OrderAsyncService orderAsyncService
    ) {
        this.cartRepository = cartRepository;
        this.clientRepository = clientRepository;
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderAsyncService = orderAsyncService;
    }

    @Override
    public CartResponse createCart(Long clientId, String email) {

        if (clientId == null) {
            throw new BadRequestException("Client id is required");
        }

        Client authenticatedClient = clientRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated client not found"));

        if (!authenticatedClient.getId().equals(clientId)) {
            throw new ForbiddenException("You cannot create a cart for another user");
        }

        if (cartRepository.existsByClientIdAndStatus(authenticatedClient.getId(), CartStatus.ACTIVE)) {
            throw new ConflictException("Client already has an active cart");
        }

        Cart cart = new Cart();
        cart.setClient(authenticatedClient);
        cart.setStatus(CartStatus.ACTIVE);
        cart.setCode(CodeGenerator.generateCartCode());

        return CartMapper.toResponse(cartRepository.save(cart));
    }

    @Override
    public CartResponse addProductToCart(String cartCode, String productCode, Integer quantity, String email) {

        if (quantity == null || quantity <= 0) {
            throw new BadRequestException("Quantity must be greater than 0");
        }

        Cart cart = cartRepository.findByCode(cartCode)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        if (!cart.getClient().getEmail().equals(email)) {
            throw new ForbiddenException("You cannot access this cart");
        }

        if (cart.getStatus() != CartStatus.ACTIVE) {
            throw new ConflictException("Cart is not active");
        }

        Product product = productRepository.findByCode(productCode)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product).orElse(null);

        int currentQuantity = (cartItem != null) ? cartItem.getQuantity() : 0;
        int totalRequested = currentQuantity + quantity;

        if (totalRequested > product.getStock()) {
            throw new ConflictException("Requested quantity exceeds available stock");
        }

        if (cartItem != null) {
            cartItem.setQuantity(totalRequested);
            cartItemRepository.save(cartItem);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            cartItemRepository.save(newItem);
        }

        return CartMapper.toResponse(cart);
    }

    @Override
    public CartResponse removeProductFromCart(String cartCode, String productCode, String email) {

        Cart cart = cartRepository.findByCode(cartCode)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        if (!cart.getClient().getEmail().equals(email)) {
            throw new ForbiddenException("You cannot access this cart");
        }

        if (cart.getStatus() != CartStatus.ACTIVE) {
            throw new ConflictException("Cart is not active");
        }

        Product product = productRepository.findByCode(productCode)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
                .orElseThrow(() -> new ResourceNotFoundException("Product is not in cart"));

        cart.getItems().remove(cartItem);
        cartItemRepository.delete(cartItem);

        return CartMapper.toResponse(cart);
    }

    @Override
    public CartDetailResponse getCartProducts(String cartCode, String email) {

        Cart cart = cartRepository.findByCode(cartCode)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        if (!cart.getClient().getEmail().equals(email)) {
            throw new ForbiddenException("You cannot access this cart");
        }

        List<CartProductResponse> products = cart.getItems().stream()
                .map(CartMapper::toCartProductResponse)
                .toList();

        CartDetailResponse response = new CartDetailResponse();
        response.setClientId(cart.getClient().getId());
        response.setCartCode(cart.getCode());
        response.setStatus(cart.getStatus().name());
        response.setProducts(products);

        return response;
    }

    @Override
    public void processCartOrder(String cartCode, String email) {
        orderAsyncService.processOrderAsync(cartCode, email);
    }

    @Override
    public List<CartResponse> getCartsByClient(String email) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

        return cartRepository.findAllByClientId(client.getId()).stream()
                .map(CartMapper::toResponse)
                .toList();
    }
}
