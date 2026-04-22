package com.fedeval.cartservicechallenge.services.impl;

import com.fedeval.cartservicechallenge.exceptions.BadRequestException;
import com.fedeval.cartservicechallenge.exceptions.ConflictException;
import com.fedeval.cartservicechallenge.exceptions.ForbiddenException;
import com.fedeval.cartservicechallenge.exceptions.ResourceNotFoundException;
import com.fedeval.cartservicechallenge.models.*;
import com.fedeval.cartservicechallenge.models.enums.CartStatus;
import com.fedeval.cartservicechallenge.models.enums.OrderStatus;
import com.fedeval.cartservicechallenge.repositories.CartRepository;
import com.fedeval.cartservicechallenge.repositories.OrderItemRepository;
import com.fedeval.cartservicechallenge.repositories.OrderRepository;
import com.fedeval.cartservicechallenge.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OrderAsyncService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    @Async
    @Transactional
    public void processOrderAsync(String cartCode, String email) {

        Cart cart = cartRepository.findByCode(cartCode)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        if (!cart.getClient().getEmail().equals(email)) {
            throw new ForbiddenException("You cannot access this cart");
        }

        if (cart.getStatus() != CartStatus.ACTIVE) {
            throw new ConflictException("Cart is not active");
        }

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();

            if (cartItem.getQuantity() > product.getStock()) {
                throw new ConflictException("Insufficient stock for product " + product.getCode());
            }
        }

        cart.setStatus(CartStatus.PROCESSING);
        cartRepository.save(cart);

        try {
            BigDecimal total = BigDecimal.ZERO;

            CustomerOrder order = CustomerOrder.builder()
                    .client(cart.getClient())
                    .date(LocalDateTime.now())
                    .orderStatus(OrderStatus.PENDING)
                    .total(BigDecimal.ZERO)
                    .build();

            CustomerOrder savedOrder = orderRepository.save(order);

            for (CartItem cartItem : cart.getItems()) {
                Product product = cartItem.getProduct();

                BigDecimal discountRate = BigDecimal.ZERO;
                if (product.getCategory() != null && product.getCategory().getDiscountRate() != null) {
                    discountRate = product.getCategory().getDiscountRate();
                }

                BigDecimal unitPrice = product.getPrice();
                BigDecimal subtotal = unitPrice
                        .subtract(unitPrice.multiply(discountRate).divide(BigDecimal.valueOf(100)))
                        .multiply(BigDecimal.valueOf(cartItem.getQuantity()));

                CustomerOrderItem orderItem = CustomerOrderItem.builder()
                        .customerOrder(savedOrder)
                        .product(product)
                        .quantity(cartItem.getQuantity())
                        .unitPrice(unitPrice)
                        .subtotal(subtotal)
                        .build();

                savedOrder.addItem(orderItem);
                orderItemRepository.save(orderItem);

                product.setStock(product.getStock() - cartItem.getQuantity());
                productRepository.save(product);

                total = total.add(subtotal);

            }

            savedOrder.setTotal(total);
            savedOrder.setOrderStatus(OrderStatus.COMPLETED);
            orderRepository.save(savedOrder);

            cart.setStatus(CartStatus.COMPLETED);
            cartRepository.save(cart);

        } catch (Exception e) {
            cart.setStatus(CartStatus.ACTIVE);
            cartRepository.save(cart);

            throw e;
        }
    }
}
