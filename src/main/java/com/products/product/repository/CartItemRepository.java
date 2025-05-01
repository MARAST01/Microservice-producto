package com.products.product.repository;

import com.products.product.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUserId(String userId);
    void deleteByUserIdAndProductId(String userId, Long productId);
    CartItem findByUserIdAndProductId(String userId, Long productId);
} 