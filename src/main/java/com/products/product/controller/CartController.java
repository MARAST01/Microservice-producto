package com.products.product.controller;

import com.products.product.entity.CartItem;
import com.products.product.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/productos/carrito")
@CrossOrigin(origins = "http://localhost:5173")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public List<CartItem> getCartItems(@RequestHeader("X-User-Id") String userId) {
        return cartService.getCartItems(userId);
    }

    @PostMapping("/{productId}")
    public ResponseEntity<CartItem> addToCart(
            @PathVariable Long productId,
            @RequestBody Map<String, Integer> request,
            @RequestHeader("X-User-Id") String userId) {
        try {
            Integer quantity = request.get("cantidad");
            CartItem cartItem = cartService.addToCart(userId, productId, quantity);
            return ResponseEntity.ok(cartItem);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{productId}")
    public ResponseEntity<CartItem> updateCartItemQuantity(
            @PathVariable Long productId,
            @RequestBody Map<String, Integer> request,
            @RequestHeader("X-User-Id") String userId) {
        try {
            Integer quantity = request.get("cantidad");
            CartItem cartItem = cartService.updateQuantity(userId, productId, quantity);
            return ResponseEntity.ok(cartItem);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> removeCartItem(
            @PathVariable Long productId,
            @RequestHeader("X-User-Id") String userId) {
        try {
            cartService.removeFromCart(userId, productId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 