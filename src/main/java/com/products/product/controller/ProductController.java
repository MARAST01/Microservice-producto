package com.products.product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.products.product.entity.Product;
import com.products.product.entity.Categoria;
import com.products.product.service.CloudinaryService;
import com.products.product.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5173/inventory-management", "http://localhost:5173/productos"}, 
             allowCredentials = "true",
             allowedHeaders = {"*", "Content-Type", "X-User-Id", "Authorization"},
             exposedHeaders = {"*"},
             methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH, RequestMethod.OPTIONS})
public class ProductController {
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    
    private final ProductService productService;
    private final CloudinaryService cloudinaryService;

    public ProductController(ProductService productService, CloudinaryService cloudinaryService) {
        this.productService = productService;
        this.cloudinaryService = cloudinaryService;
    }

    @PostMapping(value = "/crear", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Product createProduct(@RequestPart("product") String productJson,
                               @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Product product = objectMapper.readValue(productJson, Product.class);
        
        if (image != null && !image.isEmpty()) {
            String imageUrl = cloudinaryService.uploadImage(image);
            product.setImagenUrl(imageUrl);
        }
        return productService.createProduct(product);
    }

    @PutMapping("/{id}")
    public Product updateProduct(@PathVariable Long id,
                               @RequestPart("product") Product productUpdates,
                               @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {
        if (image != null && !image.isEmpty()) {
            // Si hay una imagen existente, la eliminamos
            Product existingProduct = productService.getProductById(id);
            if (existingProduct.getImagenUrl() != null) {
                cloudinaryService.deleteImage(existingProduct.getImagenUrl());
            }
            
            // Subimos la nueva imagen
            String imageUrl = cloudinaryService.uploadImage(image);
            productUpdates.setImagenUrl(imageUrl);
        }
        return productService.updateProduct(id, productUpdates);
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) throws IOException {
        Product product = productService.getProductById(id);
        if (product.getImagenUrl() != null) {
            cloudinaryService.deleteImage(product.getImagenUrl());
        }
        productService.deleteProduct(id);
    }

    @GetMapping("/{id}")
    public Product getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @GetMapping("/")
    public ResponseEntity<List<Product>> findAll() {
        try {
            logger.info("Recibida solicitud para obtener todos los productos");
            List<Product> products = productService.findAll();
            logger.info("Productos encontrados: {}", products.size());
            
            if (products == null) {
                logger.warn("La lista de productos es null");
                return ResponseEntity.ok(new ArrayList<>());
            }
            
            if (products.isEmpty()) {
                logger.warn("No se encontraron productos en la base de datos");
            } else {
                logger.info("Productos recuperados exitosamente");
                products.forEach(p -> logger.debug("Producto: id={}, nombre={}", p.getId(), p.getNombre()));
            }
            
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            logger.error("Error al obtener productos: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener los productos: " + e.getMessage());
        }
    }

    // Nuevos endpoints de filtrado
    @GetMapping("/buscar")
    public List<Product> findByNombre(@RequestParam String nombre) {
        return productService.findByNombre(nombre);
    }

    @GetMapping("/categoria/{categoria}")
    public List<Product> findByCategoria(@PathVariable Categoria categoria) {
        return productService.findByCategoria(categoria);
    }

    @GetMapping("/precio")
    public List<Product> findByPrecioRange(
            @RequestParam Double precioMin,
            @RequestParam Double precioMax) {
        return productService.findByPrecioRange(precioMin, precioMax);
    }

    @GetMapping("/{id}/disponibilidad")
    public ResponseEntity<Map<String, Object>> verificarDisponibilidad(
            @PathVariable Long id,
            @RequestParam Integer cantidad) {
        boolean disponible = productService.verificarDisponibilidad(id, cantidad);
        Product product = productService.getProductById(id);
        
        return ResponseEntity.ok(Map.of(
            "disponible", disponible,
            "stockActual", product.getCantidad(),
            "mensaje", disponible ? 
                "Producto disponible" : 
                "No hay suficiente stock disponible"
        ));
    }

    @PostMapping("/{id}/stock")
    public ResponseEntity<Map<String, Object>> actualizarStock(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> request) {
        Integer cantidad = request.get("cantidad");
        productService.actualizarStock(id, cantidad);
        
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(Map.of(
            "mensaje", "Stock actualizado correctamente",
            "stockActual", product.getCantidad()
        ));
    }

    @PostMapping("/{id}/revertir-stock")
    public ResponseEntity<Map<String, Object>> revertirStock(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> request) {
        Integer cantidad = request.get("cantidad");
        productService.revertirStock(id, cantidad);
        
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(Map.of(
            "mensaje", "Stock revertido correctamente",
            "stockActual", product.getCantidad()
        ));
    }
}