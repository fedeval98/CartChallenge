package com.fedeval.cartservicechallenge.repositories;

import com.fedeval.cartservicechallenge.models.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<CustomerOrder, Long> {
}
