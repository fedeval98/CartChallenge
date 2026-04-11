package com.fedeval.cartservicechallenge.services.impl;

import com.fedeval.cartservicechallenge.models.Cart;
import com.fedeval.cartservicechallenge.models.Client;
import com.fedeval.cartservicechallenge.models.enums.CartStatus;
import com.fedeval.cartservicechallenge.repositories.CartRepository;
import com.fedeval.cartservicechallenge.repositories.ClientRepository;
import com.fedeval.cartservicechallenge.services.CartService;
import com.fedeval.cartservicechallenge.utils.CodeGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ClientRepository clientRepository;

    public CartServiceImpl(CartRepository cartRepository, ClientRepository clientRepository) {
        this.cartRepository = cartRepository;
        this.clientRepository = clientRepository;
    }

    @Override
    public Cart createCart(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        if (cartRepository.existsByClientIdAndStatus(clientId, CartStatus.ACTIVE)) {
            throw new IllegalStateException("Client already has an active cart");
        }

        Cart cart = new Cart();
        cart.setClient(client);
        cart.setStatus(CartStatus.ACTIVE);
        cart.setCode(CodeGenerator.generateCartCode());

        return cartRepository.save(cart);
    }


}
