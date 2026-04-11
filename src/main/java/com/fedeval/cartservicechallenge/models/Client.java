package com.fedeval.cartservicechallenge.models;

import com.fedeval.cartservicechallenge.models.enums.RoleType;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


@Entity
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor

public class Client {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (nullable = false)
    private String firstName, lastName, password;

    @Column (nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RoleType role = RoleType.CLIENT;

    @OneToMany(mappedBy = "client")
    @Builder.Default
    private List<Cart> carts = new ArrayList<>();

    @OneToMany(mappedBy = "client")
    @Builder.Default
    private List<CustomerOrder> customerOrders = new ArrayList<>();

}
