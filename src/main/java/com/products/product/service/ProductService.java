package com.products.product.service;

import com.products.product.entity.Product;
import com.products.product.entity.Categoria;
import com.products.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ArrayList;

@Service
public class ProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
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
        try {
            logger.info("Intentando obtener todos los productos");
            List<Product> products = productRepository.findAll();
            logger.info("Productos encontrados: {}", products.size());
            
            if (products == null) {
                logger.warn("La lista de productos es null");
                return new ArrayList<>();
            }
            
            if (products.isEmpty()) {
                logger.warn("No se encontraron productos en la base de datos");
            } else {
                logger.info("Productos recuperados exitosamente");
                products.forEach(p -> logger.debug("Producto: id={}, nombre={}", p.getId(), p.getNombre()));
            }
            
            return products;
        } catch (Exception e) {
            logger.error("Error al obtener productos: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener los productos: " + e.getMessage());
        }
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
    public void actualizarStock(Long id, Integer cantidad) {
        Product product = getProductById(id);
        // Si la cantidad es negativa, significa que queremos restar stock
        if (cantidad < 0) {
            if (product.getCantidad() < Math.abs(cantidad)) {
                throw new RuntimeException("No hay suficiente stock disponible");
            }
            product.setCantidad(product.getCantidad() + cantidad);
        } else {
            // Si la cantidad es positiva, significa que queremos establecer un nuevo valor de stock
            product.setCantidad(cantidad);
        }
        productRepository.save(product);
    }

    @Transactional
    public void revertirStock(Long id, Integer cantidad) {
        Product product = getProductById(id);
        product.setCantidad(product.getCantidad() + cantidad);
        productRepository.save(product);
    }

    @Transactional
    public boolean verificarCompraProducto(Long productId, String userId) {
        // Aquí deberías implementar la lógica para verificar si el usuario ha comprado el producto
        // Por ahora, retornamos true para propósitos de prueba
        // En producción, deberías verificar contra tu base de datos de órdenes/pedidos
        return true;
    }
}