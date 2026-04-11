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
import com.fedeval.cartservicechallenge.services.CartService;
import com.fedeval.cartservicechallenge.utils.CodeGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;

    public CartServiceImpl(CartRepository cartRepository, ClientRepository clientRepository, ProductRepository productRepository, CartItemRepository cartItemRepository) {
        this.cartRepository = cartRepository;
        this.clientRepository = clientRepository;
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
    }

    @Override
    public Cart createCart(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

        if (cartRepository.existsByClientIdAndStatus(clientId, CartStatus.ACTIVE)) {
            throw new ConflictException("Client already has an active cart");
        }

        Cart cart = new Cart();
        cart.setClient(client);
        cart.setStatus(CartStatus.ACTIVE);
        cart.setCode(CodeGenerator.generateCartCode());

        return cartRepository.save(cart);
    }

    @Override
    public Cart addProductToCart(String cartCode, String productCode, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new BadRequestException("Quantity must be greater than 0");
        }

        Cart cart = cartRepository.findByCode(cartCode)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        if (cart.getStatus() != CartStatus.ACTIVE) {
            throw new ConflictException("Cart is not active");
        }

        Product product = productRepository.findByCode(productCode)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product).orElse(null);

        int currentQuantityInCart = (cartItem != null) ? cartItem.getQuantity() : 0;
        int totalRequested = currentQuantityInCart + quantity;

        if (totalRequested > product.getStock()) {
            throw new ConflictException("Requested quantity exceeds available stock");
        }

        if (cartItem != null) {
            cartItem.setQuantity(totalRequested);
            cartItemRepository.save(cartItem);
        } else {
            CartItem newCartItem = new CartItem();
            newCartItem.setCart(cart);
            newCartItem.setProduct(product);
            newCartItem.setQuantity(quantity);
            cartItemRepository.save(newCartItem);
        }

        return cart;
    }

}
