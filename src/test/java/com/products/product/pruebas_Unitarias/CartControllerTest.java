package com.products.product.pruebas_Unitarias;

import com.products.product.controller.CartController;
import com.products.product.entity.CartItem;
import com.products.product.entity.Product;
import com.products.product.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @Mock
    private CartService cartService;

    @InjectMocks
    private CartController cartController;

    private CartItem testCartItem;
    private List<CartItem> cartItems;

    @BeforeEach
    void setUp() {
        Product product = new Product();
        product.setId(1L);
        product.setNombre("Test Product");
        product.setPrecio(99.99);

        testCartItem = new CartItem();
        testCartItem.setId(1L);
        testCartItem.setProduct(product);
        testCartItem.setQuantity(2);
        testCartItem.setUserId("user1");

        CartItem cartItem2 = new CartItem();
        cartItem2.setId(2L);
        cartItem2.setProduct(product);
        cartItem2.setQuantity(1);
        cartItem2.setUserId("user1");

        cartItems = Arrays.asList(testCartItem, cartItem2);
    }

    @Test
    void getCartItems_ShouldReturnCartItems() {
        // Arrange
        String userId = "user1";
        when(cartService.getCartItems(userId)).thenReturn(cartItems);

        // Act
        List<CartItem> result = cartController.getCartItems(userId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(cartService).getCartItems(userId);
    }

    @Test
    void addToCart_ShouldReturnCartItem() {
        // Arrange
        Long productId = 1L;
        Integer quantity = 2;
        String userId = "user1";
        when(cartService.addToCart(userId, productId, quantity)).thenReturn(testCartItem);

        // Act
        ResponseEntity<CartItem> response = cartController.addToCart(productId, Map.of("cantidad", quantity), userId);
        CartItem result = response.getBody();

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(cartService).addToCart(userId, productId, quantity);
    }

    @Test
    void addToCart_ShouldReturnBadRequestWhenException() {
        // Arrange
        Long productId = 1L;
        Integer quantity = 2;
        String userId = "user1";
        when(cartService.addToCart(userId, productId, quantity)).thenThrow(new RuntimeException("Error"));

        // Act
        ResponseEntity<CartItem> response = cartController.addToCart(productId, Map.of("cantidad", quantity), userId);

        // Assert
        assertNotNull(response);
        assertEquals(400, response.getStatusCodeValue());
        verify(cartService).addToCart(userId, productId, quantity);
    }

    @Test
    void updateCartItemQuantity_ShouldReturnUpdatedCartItem() {
        // Arrange
        Long productId = 1L;
        Integer quantity = 3;
        String userId = "user1";
        when(cartService.updateQuantity(userId, productId, quantity)).thenReturn(testCartItem);

        // Act
        ResponseEntity<CartItem> response = cartController.updateCartItemQuantity(productId, Map.of("cantidad", quantity), userId);
        CartItem result = response.getBody();

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(cartService).updateQuantity(userId, productId, quantity);
    }

    @Test
    void updateCartItemQuantity_ShouldReturnBadRequestWhenException() {
        // Arrange
        Long productId = 1L;
        Integer quantity = 3;
        String userId = "user1";
        when(cartService.updateQuantity(userId, productId, quantity)).thenThrow(new RuntimeException("Error"));

        // Act
        ResponseEntity<CartItem> response = cartController.updateCartItemQuantity(productId, Map.of("cantidad", quantity), userId);

        // Assert
        assertNotNull(response);
        assertEquals(400, response.getStatusCodeValue());
        verify(cartService).updateQuantity(userId, productId, quantity);
    }

    @Test
    void removeCartItem_ShouldReturnOk() {
        // Arrange
        Long productId = 1L;
        String userId = "user1";
        doNothing().when(cartService).removeFromCart(userId, productId);

        // Act
        ResponseEntity<Void> response = cartController.removeCartItem(productId, userId);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify(cartService).removeFromCart(userId, productId);
    }

    @Test
    void removeCartItem_ShouldReturnBadRequestWhenException() {
        // Arrange
        Long productId = 1L;
        String userId = "user1";
        doThrow(new RuntimeException("Error")).when(cartService).removeFromCart(userId, productId);

        // Act
        ResponseEntity<Void> response = cartController.removeCartItem(productId, userId);

        // Assert
        assertNotNull(response);
        assertEquals(400, response.getStatusCodeValue());
        verify(cartService).removeFromCart(userId, productId);
    }
}
