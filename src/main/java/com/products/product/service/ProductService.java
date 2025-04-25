package com.products.product.service;

import com.products.product.entity.Product;
import com.products.product.entity.Categoria;
import com.products.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            if (updates.getImagenUrl() != null) {
                existingProduct.setImagenUrl(updates.getImagenUrl());
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

    // Nuevos métodos de filtrado
    public List<Product> findByNombre(String nombre) {
        return productRepository.findByNombreContainingIgnoreCase(nombre);
    }

    public List<Product> findByCategoria(Categoria categoria) {
        return productRepository.findByCategoria(categoria);
    }

    public List<Product> findByPrecioRange(Double precioMin, Double precioMax) {
        return productRepository.findByPrecioBetween(precioMin, precioMax);
    }

    @Transactional
    public boolean verificarDisponibilidad(Long id, Integer cantidadSolicitada) {
        Product product = getProductById(id);
        return product.getCantidad() >= cantidadSolicitada;
    }

    @Transactional
    public void actualizarStock(Long id, Integer cantidadVendida) {
        Product product = getProductById(id);
        if (product.getCantidad() < cantidadVendida) {
            throw new RuntimeException("No hay suficiente stock disponible");
        }
        product.setCantidad(product.getCantidad() - cantidadVendida);
        productRepository.save(product);
    }

    @Transactional
    public void revertirStock(Long id, Integer cantidad) {
        Product product = getProductById(id);
        product.setCantidad(product.getCantidad() + cantidad);
        productRepository.save(product);
    }
}