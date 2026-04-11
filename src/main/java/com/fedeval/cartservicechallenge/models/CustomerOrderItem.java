package com.fedeval.cartservicechallenge.models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class CustomerOrderItem {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(optional = false)
        @JoinColumn(name = "customer_order_id")
        private CustomerOrder customerOrder;

        @ManyToOne(optional = false)
        @JoinColumn(name = "product_id")
        private Product product;

        @Column(nullable = false)
        private Integer quantity;

        @Column(nullable = false)
        private BigDecimal unitPrice;

        @Column(nullable = false)
        private BigDecimal subtotal;
}
