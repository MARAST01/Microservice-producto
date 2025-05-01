package com.products.product.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cart_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private Integer quantity;

    @Column(name = "user_id")
    private String userId; // Para identificar a qué usuario pertenece el carrito
} 