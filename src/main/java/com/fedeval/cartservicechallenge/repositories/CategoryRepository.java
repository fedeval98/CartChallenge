package com.fedeval.cartservicechallenge.repositories;

import com.fedeval.cartservicechallenge.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
