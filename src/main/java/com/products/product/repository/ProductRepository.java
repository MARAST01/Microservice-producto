package com.products.product.repository;

import com.products.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsByNombre(String nombre);
    boolean existsById(Long id);
}