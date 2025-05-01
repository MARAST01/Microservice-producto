package com.products.product.repository;

import com.products.product.entity.Product;
import com.products.product.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsByNombre(String nombre);
    boolean existsById(Long id);
    
    // Filtros
    List<Product> findByNombreContainingIgnoreCase(String nombre);
    List<Product> findByCategoria(Categoria categoria);
    List<Product> findByPrecioBetween(Double precioMin, Double precioMax);
}