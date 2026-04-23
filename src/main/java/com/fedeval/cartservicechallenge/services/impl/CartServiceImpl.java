package com.fedeval.cartservicechallenge.services.impl;

import com.fedeval.cartservicechallenge.dtos.cart.request.CartProductItemRequest;
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

        Cart cart = Cart.builder()
                .client(authenticatedClient)
                .status(CartStatus.ACTIVE)
                .code(CodeGenerator.generateCartCode())
                .build();

        authenticatedClient.addCart(cart);

        return CartMapper.toResponse(cartRepository.save(cart));
    }

    @Override
    public CartResponse addProductsToCart(String cartCode, List<CartProductItemRequest> products, String email) {

        Cart cart = cartRepository.findByCode(cartCode)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        if (!cart.getClient().getEmail().equals(email)) {
            throw new ForbiddenException("You cannot access this cart");
        }

        if (cart.getStatus() != CartStatus.ACTIVE) {
            throw new ConflictException("Cart is not active");
        }

        for (CartProductItemRequest requestItem : products) {

            if (requestItem.getQuantity() == null || requestItem.getQuantity() <= 0) {
                throw new BadRequestException("Quantity must be greater than 0");
            }

            Product product = productRepository.findByCode(requestItem.getProductCode())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found: " + requestItem.getProductCode()
                    ));

            CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product).orElse(null);

            int currentQuantity = (cartItem != null) ? cartItem.getQuantity() : 0;
            int totalRequested = currentQuantity + requestItem.getQuantity();

            if (totalRequested > product.getStock()) {
                throw new ConflictException("Requested quantity exceeds available stock for product " + product.getCode());
            }

            if (cartItem != null) {
                cartItem.setQuantity(totalRequested);
                cartItemRepository.save(cartItem);
            } else {
                CartItem newItem = CartItem.builder()
                        .cart(cart)
                        .product(product)
                        .quantity(requestItem.getQuantity())
                        .build();

                cartItemRepository.save(newItem);
            }
        }

        return CartMapper.toResponse(cartRepository.findByCode(cartCode)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found")));
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

        cart.removeItem(cartItem);
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

        return CartDetailResponse.builder()
                .clientId(cart.getClient().getId())
                .cartCode(cart.getCode())
                .status(cart.getStatus().name())
                .products(products)
                .build();
    }

    @Override
    public void processCartOrder(String cartCode, String email) {

        Cart cart = cartRepository.findByCode(cartCode)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        if (!cart.getClient().getEmail().equals(email)) {
            throw new ForbiddenException("You cannot access this cart");
        }

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        orderAsyncService.processOrderAsync(cartCode, email);
    }

    @Override
    public List<CartResponse> getCartsByClient(String email) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

        List<Cart> carts = cartRepository.findAllByClientId(client.getId());

        if (carts.isEmpty()) {
            throw new ResourceNotFoundException("There are no carts associated with this client yet");
        }

        return carts.stream()
                .map(CartMapper::toResponse)
                .toList();
    }
}
