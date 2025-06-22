package com.products.product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.products.product.entity.CartItem;
import com.products.product.entity.Product;
import com.products.product.service.CartService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    @Autowired
    private ObjectMapper objectMapper;

    private final String userId = "test-user";

    @Test
    void getCartItems_shouldReturnCartItems() throws Exception {
        Product product = new Product();
        product.setId(100L);

        CartItem item = new CartItem();
        item.setId(1L);
        item.setUserId(userId);
        item.setProduct(product);
        item.setQuantity(2);

        when(cartService.getCartItems(userId)).thenReturn(Collections.singletonList(item));

        mockMvc.perform(get("/api/productos/carrito")
                        .header("X-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].product.id").value(100L));
    }

    @Test
    void addToCart_shouldReturnAddedItem() throws Exception {
        Product product = new Product();
        product.setId(100L);

        CartItem item = new CartItem();
        item.setId(1L);
        item.setUserId(userId);
        item.setProduct(product);
        item.setQuantity(1);

        when(cartService.addToCart(eq(userId), eq(100L), eq(1))).thenReturn(item);

        mockMvc.perform(post("/api/productos/carrito/{productId}", 100L)
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("cantidad", 1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.quantity").value(1));
    }

    @Test
    void updateCartItemQuantity_shouldReturnUpdatedItem() throws Exception {
        Product product = new Product();
        product.setId(100L);

        CartItem item = new CartItem();
        item.setId(1L);
        item.setUserId(userId);
        item.setProduct(product);
        item.setQuantity(5);

        when(cartService.updateQuantity(eq(userId), eq(100L), eq(5))).thenReturn(item);

        mockMvc.perform(put("/api/productos/carrito/{productId}", 100L)
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("cantidad", 5))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.quantity").value(5));
    }

    @Test
    void removeCartItem_shouldReturnOk() throws Exception {
        doNothing().when(cartService).removeFromCart(userId, 100L);

        mockMvc.perform(delete("/api/productos/carrito/{productId}", 100L)
                        .header("X-User-Id", userId))
                .andExpect(status().isOk());
    }

    @Test
    void addToCart_whenProductNotFound_shouldReturnBadRequest() throws Exception {
        when(cartService.addToCart(eq(userId), eq(999L), eq(1)))
                .thenThrow(new RuntimeException("Product not found"));

        mockMvc.perform(post("/api/productos/carrito/{productId}", 999L)
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("cantidad", 1))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCartItemQuantity_whenProductNotInCart_shouldReturnBadRequest() throws Exception {
        when(cartService.updateQuantity(eq(userId), eq(999L), eq(5)))
                .thenThrow(new RuntimeException("Product not in cart"));

        mockMvc.perform(put("/api/productos/carrito/{productId}", 999L)
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("cantidad", 5))))
                .andExpect(status().isBadRequest());
    }
} 