package com.fedeval.cartservicechallenge.repositories;

import com.fedeval.cartservicechallenge.models.Cart;
import com.fedeval.cartservicechallenge.models.enums.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartRepository extends JpaRepository<Cart, Long> {
    List<Cart> findByClientId(Long clientId);
    boolean existsByClientIdAndStatus(Long clientId, CartStatus status);
}
