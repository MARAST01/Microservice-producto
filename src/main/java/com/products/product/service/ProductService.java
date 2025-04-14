package com.products.product.service;

import com.products.product.entity.Product;
import com.products.product.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Product updateProduct(Long id, Product updates) {
        return productRepository.findById(id).map(existingProduct -> {
            if (updates.getNombre() != null) {
                existingProduct.setNombre(updates.getNombre());
            }
            if (updates.getCategoria() != null) {
                existingProduct.setCategoria(updates.getCategoria());
            }
            if (updates.getPrecio() != null) {
                existingProduct.setPrecio(updates.getPrecio());
            }
            if (updates.getCantidad() != null) {
                existingProduct.setCantidad(updates.getCantidad());
            }
            if (updates.getDescripcion() != null) {
                existingProduct.setDescripcion(updates.getDescripcion());
            }
            return productRepository.save(existingProduct);
        }).orElseThrow(() -> new RuntimeException("Producto no encontrado"));
    }

    public void deleteProduct(Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
        } else {
            throw new RuntimeException("Producto no encontrado con ID: " + id);
        }
    }
}