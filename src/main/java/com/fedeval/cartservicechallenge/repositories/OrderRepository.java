package com.fedeval.cartservicechallenge.repositories;

import com.fedeval.cartservicechallenge.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByClientId(Long clientId);
}
