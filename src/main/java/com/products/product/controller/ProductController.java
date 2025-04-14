package com.products.product.controller;

import com.products.product.entity.Product;
import com.products.product.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @CrossOrigin(origins = "http://localhost:5173/productos")
    @PostMapping("/crear")
    public Product createProduct(@RequestBody Product product) {
        return productService.createProduct(product);
    }

    @GetMapping("/{id}")
    public Product getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @PutMapping("/{id}")
    public Product updateProduct(@PathVariable Long id, @RequestBody Product productUpdates) {
        return productService.updateProduct(id, productUpdates);
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
    }

    @GetMapping
    public List<Product> findAll() {
        return productService.findAll();
    }
}