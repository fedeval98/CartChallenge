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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OrderAsyncService {

    private static final Logger log = LoggerFactory.getLogger(OrderAsyncService.class);

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    @Async
    @Transactional
    public void processOrderAsync(String cartCode, String email) {
        log.info("Starting async order processing for cart {}", cartCode);

        Cart cart = cartRepository.findByCode(cartCode)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        if (!cart.getClient().getEmail().equals(email)) {
            log.warn("Forbidden access to cart {} by user {}", cartCode, email);
            throw new ForbiddenException("You cannot access this cart");
        }

        if (cart.getStatus() != CartStatus.ACTIVE) {
            log.warn("Cart {} is not active. Current status: {}", cartCode, cart.getStatus());
            throw new ConflictException("Cart is not active");
        }

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            log.warn("Cart {} is empty", cartCode);
            throw new BadRequestException("Cart is empty");
        }

        cart.setStatus(CartStatus.PROCESSING);
        cartRepository.save(cart);
        log.info("Cart {} changed to PROCESSING", cartCode);

        try {
            BigDecimal total = BigDecimal.ZERO;

            CustomerOrder order = new CustomerOrder();
            order.setClient(cart.getClient());
            order.setDate(LocalDateTime.now());
            order.setOrderStatus(OrderStatus.PENDING);
            order.setTotal(BigDecimal.ZERO);

            CustomerOrder savedOrder = orderRepository.save(order);
            log.info("Order created in PENDING status for cart {}", cartCode);

            for (CartItem cartItem : cart.getItems()) {
                Product product = cartItem.getProduct();

                if (cartItem.getQuantity() > product.getStock()) {
                    log.warn(
                            "Insufficient stock for product {} in cart {}. Requested: {}, Available: {}",
                            product.getCode(),
                            cartCode,
                            cartItem.getQuantity(),
                            product.getStock()
                    );
                    throw new ConflictException(
                            "Insufficient stock for product: " + product.getCode()
                    );
                }

                BigDecimal discountRate = BigDecimal.ZERO;
                if (product.getCategory() != null && product.getCategory().getDiscountRate() != null) {
                    discountRate = product.getCategory().getDiscountRate();
                }

                BigDecimal unitPrice = product.getPrice();
                BigDecimal subtotal = unitPrice
                        .subtract(unitPrice.multiply(discountRate).divide(BigDecimal.valueOf(100)))
                        .multiply(BigDecimal.valueOf(cartItem.getQuantity()));

                CustomerOrderItem orderItem = new CustomerOrderItem();
                orderItem.setCustomerOrder(savedOrder);
                orderItem.setProduct(product);
                orderItem.setQuantity(cartItem.getQuantity());
                orderItem.setUnitPrice(unitPrice);
                orderItem.setSubtotal(subtotal);

                orderItemRepository.save(orderItem);

                product.setStock(product.getStock() - cartItem.getQuantity());
                productRepository.save(product);

                total = total.add(subtotal);

                log.info(
                        "Processed product {} for cart {}. Quantity: {}, Remaining stock: {}",
                        product.getCode(),
                        cartCode,
                        cartItem.getQuantity(),
                        product.getStock()
                );
            }

            savedOrder.setTotal(total);
            savedOrder.setOrderStatus(OrderStatus.COMPLETED);
            orderRepository.save(savedOrder);
            log.info("Order for cart {} changed to COMPLETED with total {}", cartCode, total);

            cart.setStatus(CartStatus.COMPLETED);
            cartRepository.save(cart);
            log.info("Cart {} changed to COMPLETED", cartCode);

            log.info("Async order processing finished successfully for cart {}", cartCode);

        } catch (Exception e) {
            cart.setStatus(CartStatus.ACTIVE);
            cartRepository.save(cart);

            log.error("Error processing cart {}: {}", cartCode, e.getMessage(), e);
            log.info("Cart {} rolled back to ACTIVE", cartCode);

            throw e;
        }
    }
}
