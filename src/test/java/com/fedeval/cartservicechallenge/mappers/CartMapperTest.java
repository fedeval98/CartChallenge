package com.fedeval.cartservicechallenge.mappers;

import com.fedeval.cartservicechallenge.dtos.cart.response.CartProductResponse;
import com.fedeval.cartservicechallenge.dtos.cart.response.CartResponse;
import com.fedeval.cartservicechallenge.models.*;
import com.fedeval.cartservicechallenge.models.enums.CartStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CartMapperTest {
    @Test
    void should_map_cart_to_response() {
        Client client = new Client();
        client.setId(1L);

        Product product = new Product();
        product.setCode("P1");

        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(2);

        Cart cart = new Cart();
        cart.setCode("CART123");
        cart.setClient(client);
        cart.setStatus(CartStatus.ACTIVE);
        cart.setItems(List.of(item));

        CartResponse response = CartMapper.toResponse(cart);

        assertEquals("CART123", response.getCode());
        assertEquals(1L, response.getClientId());
        assertEquals("ACTIVE", response.getStatus());

        assertEquals(1, response.getItems().size());
        assertEquals("P1", response.getItems().get(0).getProductCode());
        assertEquals(2, response.getItems().get(0).getQuantity());
    }

    @Test
    void should_map_cart_item_with_category_and_discount() {
        Category category = new Category();
        category.setName("Electronics");
        category.setDiscountRate(BigDecimal.valueOf(10)); // 10%

        Product product = new Product();
        product.setId(1L);
        product.setCode("P1");
        product.setName("Mouse");
        product.setPrice(BigDecimal.valueOf(100));
        product.setStock(50);
        product.setCategory(category);

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(3);

        CartProductResponse response = CartMapper.toCartProductResponse(cartItem);

        assertEquals(1L, response.getProductId());
        assertEquals("P1", response.getProductCode());
        assertEquals("Mouse", response.getProductName());
        assertEquals(BigDecimal.valueOf(100), response.getPrice());
        assertEquals(50, response.getStock());
        assertEquals(3, response.getQuantity());

        assertEquals("Electronics", response.getCategoryName());
        assertEquals(BigDecimal.valueOf(10), response.getDiscountRate());

        assertEquals(BigDecimal.valueOf(90), response.getFinalPrice());
    }
}
