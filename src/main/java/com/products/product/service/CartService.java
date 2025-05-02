package com.products.product.service;

import com.products.product.entity.CartItem;
import com.products.product.entity.Product;
import com.products.product.repository.CartItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductService productService;

    public CartService(CartItemRepository cartItemRepository, ProductService productService) {
        this.cartItemRepository = cartItemRepository;
        this.productService = productService;
    }

    public List<CartItem> getCartItems(String userId) {
        return cartItemRepository.findByUserId(userId);
    }

    @Transactional
    public CartItem addToCart(String userId, Long productId, Integer quantity) {
        Product product = productService.getProductById(productId);
        
        // Verificar disponibilidad
        if (!productService.verificarDisponibilidad(productId, quantity)) {
            throw new RuntimeException("No hay suficiente stock disponible");
        }

        CartItem existingItem = cartItemRepository.findByUserIdAndProductId(userId, productId);
        
        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            return cartItemRepository.save(existingItem);
        } else {
            CartItem newItem = new CartItem();
            newItem.setUserId(userId);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            return cartItemRepository.save(newItem);
        }
    }

    @Transactional
    public CartItem updateQuantity(String userId, Long productId, Integer quantity) {
        CartItem cartItem = cartItemRepository.findByUserIdAndProductId(userId, productId);
        if (cartItem == null) {
            throw new RuntimeException("Item no encontrado en el carrito");
        }

        // Verificar disponibilidad
        if (!productService.verificarDisponibilidad(productId, quantity)) {
            throw new RuntimeException("No hay suficiente stock disponible");
        }

        cartItem.setQuantity(quantity);
        return cartItemRepository.save(cartItem);
    }

    @Transactional
    public void removeFromCart(String userId, Long productId) {
        cartItemRepository.deleteByUserIdAndProductId(userId, productId);
    }
} 