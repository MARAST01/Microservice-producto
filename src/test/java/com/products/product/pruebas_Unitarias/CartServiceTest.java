package com.products.product.pruebas_Unitarias;

import com.products.product.entity.CartItem;
import com.products.product.entity.Product;
import com.products.product.repository.CartItemRepository;
import com.products.product.service.CartService;
import com.products.product.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private CartService cartService;

    private CartItem testCartItem;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setNombre("Test Product");
        testProduct.setPrecio(99.99);
        testProduct.setCantidad(10);

        testCartItem = new CartItem();
        testCartItem.setId(1L);
        testCartItem.setProduct(testProduct);
        testCartItem.setQuantity(2);
        testCartItem.setUserId("user1");
    }

    @Test
    void getCartItems_ShouldReturnUserCartItems() {
        // Arrange
        String userId = "user1";
        when(cartItemRepository.findByUserId(userId)).thenReturn(List.of(testCartItem));

        // Act
        List<CartItem> result = cartService.getCartItems(userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCartItem, result.get(0));
        verify(cartItemRepository).findByUserId(userId);
    }

    @Test
    @Transactional
    void addToCart_ShouldAddNewItemWhenNotExists() {
        // Arrange
        String userId = "user1";
        Long productId = 1L;
        Integer quantity = 2;

        when(productService.getProductById(productId)).thenReturn(testProduct);
        when(productService.verificarDisponibilidad(productId, quantity)).thenReturn(true);
        when(cartItemRepository.findByUserIdAndProductId(userId, productId)).thenReturn(null);
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CartItem result = cartService.addToCart(userId, productId, quantity);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(productId, result.getProduct().getId());
        assertEquals(quantity, result.getQuantity());
        verify(productService).verificarDisponibilidad(productId, quantity);
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    @Transactional
    void addToCart_ShouldUpdateQuantityWhenItemExists() {
        // Arrange
        String userId = "user1";
        Long productId = 1L;
        Integer newQuantity = 3;

        when(productService.verificarDisponibilidad(productId, newQuantity)).thenReturn(true);
        when(cartItemRepository.findByUserIdAndProductId(userId, productId)).thenReturn(testCartItem);
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> {
            CartItem saved = invocation.getArgument(0);
            testCartItem.setQuantity(saved.getQuantity());
            return testCartItem;
        });

        // Act
        CartItem result = cartService.addToCart(userId, productId, newQuantity);

        // Assert
        assertNotNull(result);
        assertEquals(5, result.getQuantity()); // 2 existentes + 3 nuevos
        verify(productService).verificarDisponibilidad(productId, newQuantity);
        verify(cartItemRepository).save(testCartItem);
    }

    @Test
    @Transactional
    void addToCart_ShouldThrowWhenNotEnoughStock() {
        // Arrange
        String userId = "user1";
        Long productId = 1L;
        Integer quantity = 15;

        when(productService.verificarDisponibilidad(productId, quantity)).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> cartService.addToCart(userId, productId, quantity));
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    @Transactional
    void updateQuantity_ShouldUpdateItemQuantity() {
        // Arrange
        String userId = "user1";
        Long productId = 1L;
        Integer newQuantity = 5;

        when(productService.verificarDisponibilidad(productId, newQuantity)).thenReturn(true);
        when(cartItemRepository.findByUserIdAndProductId(userId, productId)).thenReturn(testCartItem);
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> {
            CartItem saved = invocation.getArgument(0);
            testCartItem.setQuantity(saved.getQuantity());
            return testCartItem;
        });

        // Act
        CartItem result = cartService.updateQuantity(userId, productId, newQuantity);

        // Assert
        assertNotNull(result);
        assertEquals(newQuantity, result.getQuantity());
        verify(productService).verificarDisponibilidad(productId, newQuantity);
        verify(cartItemRepository).save(testCartItem);
    }

    @Test
    @Transactional
    void updateQuantity_ShouldThrowWhenItemNotFound() {
        // Arrange
        String userId = "user1";
        Long productId = 1L;
        Integer quantity = 5;

        when(cartItemRepository.findByUserIdAndProductId(userId, productId)).thenReturn(null);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> cartService.updateQuantity(userId, productId, quantity));
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    @Transactional
    void removeFromCart_ShouldDeleteItem() {
        // Arrange
        String userId = "user1";
        Long productId = 1L;
        doNothing().when(cartItemRepository).deleteByUserIdAndProductId(userId, productId);

        // Act
        cartService.removeFromCart(userId, productId);

        // Assert
        verify(cartItemRepository).deleteByUserIdAndProductId(userId, productId);
    }
}