package com.fedeval.cartservicechallenge.repositories;

import com.fedeval.cartservicechallenge.models.Cart;
import com.fedeval.cartservicechallenge.models.enums.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    boolean existsByClientIdAndStatus(Long clientId, CartStatus status);
    Optional<Cart> findByCode(String code);
}
