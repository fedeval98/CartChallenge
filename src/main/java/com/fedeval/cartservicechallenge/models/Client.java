package com.fedeval.cartservicechallenge.models;

import com.fedeval.cartservicechallenge.models.enums.RoleType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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

    @NotBlank(message = "First name cannot be empty")
    @Column(nullable = false)
    private String firstName;

    @NotBlank(message = "Last name cannot be empty")
    @Column(nullable = false)
    private String lastName;

    @NotBlank(message = "Password cannot be empty")
    @Column(nullable = false)
    private String password;

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Email format is invalid")
    @Column(nullable = false, unique = true)
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

    public void addCart(Cart cart) {
        cart.setClient(this);
        this.carts.add(cart);
    }

    public void addOrder(CustomerOrder order) {
        order.setClient(this);
        this.customerOrders.add(order);
    }


}
