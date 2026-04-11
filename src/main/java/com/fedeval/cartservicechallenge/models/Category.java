package com.fedeval.cartservicechallenge.models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (nullable = false, unique = true)
    private String name;

    @Column (nullable = false)
    private BigDecimal discountRate;

    @OneToMany (mappedBy = "category")
    @Builder.Default
    private Set<Product> products = new HashSet<>();
}
