package com.fedeval.cartservicechallenge.repositories;

import com.fedeval.cartservicechallenge.models.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartCodeAndProductCode(String cartCode, String productCode);
    List<CartItem> findByCartCode(String code);
}
