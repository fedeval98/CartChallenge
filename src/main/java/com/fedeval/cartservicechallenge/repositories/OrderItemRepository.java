package com.fedeval.cartservicechallenge.repositories;

import com.fedeval.cartservicechallenge.models.CustomerOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<CustomerOrderItem, Long> {
}
