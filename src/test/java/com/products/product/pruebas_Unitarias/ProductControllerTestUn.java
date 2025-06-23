package com.products.product.pruebas_Unitarias;


import com.products.product.controller.ProductController;
import com.products.product.entity.Product;
import com.products.product.entity.Categoria;
import com.products.product.service.CloudinaryService;
import com.products.product.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTestUn {

    @Mock
    private ProductService productService;

    @Mock
    private CloudinaryService cloudinaryService;

    @InjectMocks
    private ProductController productController;

    private Product testProduct;
    private List<Product> productList;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setNombre("Test Product");
        testProduct.setCategoria(Categoria.ELECTRONICA);
        testProduct.setPrecio(999.99);
        testProduct.setCantidad(10);
        testProduct.setDescripcion("Test Description");

        Product product2 = new Product();
        product2.setId(2L);
        product2.setNombre("Another Product");
        product2.setCategoria(Categoria.ROPA);
        product2.setPrecio(49.99);
        product2.setCantidad(5);

        productList = Arrays.asList(testProduct, product2);
    }

    @Test
    void createProduct_WithImage_ShouldReturnProduct() throws IOException {
        // Arrange
        String productJson = "{\"nombre\":\"Test Product\",\"categoria\":\"ELECTRONICA\",\"precio\":999.99,\"cantidad\":10,\"descripcion\":\"Test Description\"}";
        MockMultipartFile image = new MockMultipartFile("image", "test.jpg", "image/jpeg", "test image content".getBytes());
        String imageUrl = "http://cloudinary.com/test.jpg";

        // Configurar el producto que se espera que se cree
        Product productWithImage = new Product();
        productWithImage.setId(1L);
        productWithImage.setNombre("Test Product");
        productWithImage.setImagenUrl(imageUrl); // Asegurar que la URL está incluida

        when(cloudinaryService.uploadImage(any(MultipartFile.class))).thenReturn(imageUrl);

        // Aquí está el cambio clave: debemos asegurarnos de que el mock devuelva el producto con la URL
        when(productService.createProduct(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            p.setImagenUrl(imageUrl); // Establecer la URL en el producto
            return productWithImage;
        });

        // Act
        Product result = productController.createProduct(productJson, image);

        // Assert
        assertNotNull(result);
        assertEquals(testProduct.getId(), result.getId());
        assertEquals(imageUrl, result.getImagenUrl()); // Esta aserción debería pasar ahora
        verify(cloudinaryService).uploadImage(any(MultipartFile.class));
        verify(productService).createProduct(any(Product.class));
    }

    @Test
    void createProduct_WithoutImage_ShouldReturnProduct() throws IOException {
        // Arrange
        String productJson = "{\"nombre\":\"Test Product\",\"categoria\":\"ELECTRONICA\",\"precio\":999.99,\"cantidad\":10,\"descripcion\":\"Test Description\"}";

        when(productService.createProduct(any(Product.class))).thenReturn(testProduct);

        // Act
        Product result = productController.createProduct(productJson, null);

        // Assert
        assertNotNull(result);
        assertEquals(testProduct.getId(), result.getId());
        assertNull(result.getImagenUrl());
        verify(cloudinaryService, never()).uploadImage(any());
        verify(productService).createProduct(any(Product.class));
    }

    @Test
    void updateProduct_WithNewImage_ShouldReturnUpdatedProduct() throws IOException {
        // Arrange
        Long productId = 1L;
        Product updates = new Product();
        updates.setNombre("Updated Product");
        updates.setCantidad(15);

        MockMultipartFile image = new MockMultipartFile("image", "test.jpg", "image/jpeg", "test image content".getBytes());
        String oldImageUrl = "http://cloudinary.com/old.jpg";
        String newImageUrl = "http://cloudinary.com/new.jpg";

        Product existingProduct = new Product();
        existingProduct.setImagenUrl(oldImageUrl);

        when(productService.getProductById(productId)).thenReturn(existingProduct);
        when(cloudinaryService.uploadImage(any(MultipartFile.class))).thenReturn(newImageUrl);
        when(productService.updateProduct(eq(productId), any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(1);
            existingProduct.setNombre(p.getNombre());
            existingProduct.setCantidad(p.getCantidad());
            existingProduct.setImagenUrl(p.getImagenUrl());
            return existingProduct;
        });

        // Act
        Product result = productController.updateProduct(productId, updates, image);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Product", result.getNombre());
        assertEquals(15, result.getCantidad());
        assertEquals(newImageUrl, result.getImagenUrl());
        verify(cloudinaryService).deleteImage(oldImageUrl);
        verify(cloudinaryService).uploadImage(image);
        verify(productService).updateProduct(eq(productId), any(Product.class));
    }

    @Test
    void updateProduct_WithoutImage_ShouldReturnUpdatedProduct() throws IOException {
        // Arrange
        Long productId = 1L;
        Product updates = new Product();
        updates.setNombre("Updated Product");
        updates.setCantidad(15);

        Product existingProduct = new Product();
        existingProduct.setImagenUrl("http://cloudinary.com/existing.jpg");

        when(productService.updateProduct(eq(productId), any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(1);
            existingProduct.setNombre(p.getNombre());
            existingProduct.setCantidad(p.getCantidad());
            return existingProduct;
        });

        // Act
        Product result = productController.updateProduct(productId, updates, null);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Product", result.getNombre());
        assertEquals(15, result.getCantidad());
        assertEquals("http://cloudinary.com/existing.jpg", result.getImagenUrl());
        verify(cloudinaryService, never()).deleteImage(any());
        verify(cloudinaryService, never()).uploadImage(any());
        verify(productService).updateProduct(eq(productId), any(Product.class));
    }

    @Test
    void deleteProduct_WithImage_ShouldDeleteProductAndImage() throws IOException {
        // Arrange
        Long productId = 1L;
        Product product = new Product();
        product.setImagenUrl("http://cloudinary.com/test.jpg");

        when(productService.getProductById(productId)).thenReturn(product);
        doNothing().when(cloudinaryService).deleteImage(anyString());
        doNothing().when(productService).deleteProduct(productId);

        // Act
        productController.deleteProduct(productId);

        // Assert
        verify(productService).getProductById(productId);
        verify(cloudinaryService).deleteImage("http://cloudinary.com/test.jpg");
        verify(productService).deleteProduct(productId);
    }

    @Test
    void deleteProduct_WithoutImage_ShouldDeleteProductOnly() throws IOException {
        // Arrange
        Long productId = 1L;
        Product product = new Product();
        product.setImagenUrl(null);

        when(productService.getProductById(productId)).thenReturn(product);
        doNothing().when(productService).deleteProduct(productId);

        // Act
        productController.deleteProduct(productId);

        // Assert
        verify(productService).getProductById(productId);
        verify(cloudinaryService, never()).deleteImage(any());
        verify(productService).deleteProduct(productId);
    }

    @Test
    void getProductById_ShouldReturnProduct() {
        // Arrange
        Long productId = 1L;
        when(productService.getProductById(productId)).thenReturn(testProduct);

        // Act
        Product result = productController.getProductById(productId);

        // Assert
        assertNotNull(result);
        assertEquals(productId, result.getId());
        verify(productService).getProductById(productId);
    }

    @Test
    void findAll_ShouldReturnAllProducts() {
        // Arrange
        when(productService.findAll()).thenReturn(productList);

        // Act
        ResponseEntity<List<Product>> response = productController.findAll();
        List<Product> result = response.getBody();

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(productService).findAll();
    }

    @Test
    void findByNombre_ShouldReturnMatchingProducts() {
        // Arrange
        String searchTerm = "Test";
        when(productService.findByNombre(searchTerm)).thenReturn(List.of(testProduct));

        // Act
        List<Product> result = productController.findByNombre(searchTerm);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Product", result.get(0).getNombre());
        verify(productService).findByNombre(searchTerm);
    }

    @Test
    void findByCategoria_ShouldReturnCategoryProducts() {
        // Arrange
        Categoria categoria = Categoria.ELECTRONICA;
        when(productService.findByCategoria(categoria)).thenReturn(List.of(testProduct));

        // Act
        List<Product> result = productController.findByCategoria(categoria);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Categoria.ELECTRONICA, result.get(0).getCategoria());
        verify(productService).findByCategoria(categoria);
    }

    @Test
    void findByPrecioRange_ShouldReturnProductsInRange() {
        // Arrange
        Double min = 900.0;
        Double max = 1000.0;
        when(productService.findByPrecioRange(min, max)).thenReturn(List.of(testProduct));

        // Act
        List<Product> result = productController.findByPrecioRange(min, max);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(999.99, result.get(0).getPrecio());
        verify(productService).findByPrecioRange(min, max);
    }

    @Test
    void verificarDisponibilidad_ShouldReturnAvailability() {
        // Arrange
        Long productId = 1L;
        Integer cantidad = 5;
        when(productService.verificarDisponibilidad(productId, cantidad)).thenReturn(true);
        when(productService.getProductById(productId)).thenReturn(testProduct);

        // Act
        ResponseEntity<Map<String, Object>> response = productController.verificarDisponibilidad(productId, cantidad);
        Map<String, Object> result = response.getBody();

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(result);
        assertTrue((Boolean) result.get("disponible"));
        assertEquals(10, result.get("stockActual"));
        verify(productService).verificarDisponibilidad(productId, cantidad);
    }

    @Test
    void actualizarStock_ShouldUpdateStock() {
        // Arrange
        Long productId = 1L;
        Integer cantidad = 15;
        when(productService.getProductById(productId)).thenReturn(testProduct);

        // Act
        ResponseEntity<Map<String, Object>> response = productController.actualizarStock(productId, Map.of("cantidad", cantidad));
        Map<String, Object> result = response.getBody();

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(result);
        assertEquals("Stock actualizado correctamente", result.get("mensaje"));
        verify(productService).actualizarStock(productId, cantidad);
    }

    @Test
    void revertirStock_ShouldRevertStock() {
        // Arrange
        Long productId = 1L;
        Integer cantidad = 5;
        when(productService.getProductById(productId)).thenReturn(testProduct);

        // Act
        ResponseEntity<Map<String, Object>> response = productController.revertirStock(productId, Map.of("cantidad", cantidad));
        Map<String, Object> result = response.getBody();

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(result);
        assertEquals("Stock revertido correctamente", result.get("mensaje"));
        verify(productService).revertirStock(productId, cantidad);
    }
}