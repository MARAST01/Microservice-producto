package com.products.product.pruebas_Unitarias;

import com.products.product.entity.Product;
import com.products.product.entity.Categoria;
import com.products.product.repository.ProductRepository;
import com.products.product.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setNombre("Test Product");
        testProduct.setCategoria(Categoria.ELECTRONICA);
        testProduct.setPrecio(999.99);
        testProduct.setCantidad(10);
        testProduct.setDescripcion("Test Description");
    }

    @Test
    void createProduct_ShouldReturnSavedProduct() {
        // Arrange
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        Product result = productService.createProduct(testProduct);

        // Assert
        assertNotNull(result);
        assertEquals(testProduct.getId(), result.getId());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void getProductById_ShouldReturnProduct() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // Act
        Product result = productService.getProductById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(productRepository).findById(1L);
    }

    @Test
    void getProductById_ShouldThrowExceptionWhenNotFound() {
        // Arrange
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> productService.getProductById(1L));
        verify(productRepository).findById(1L);
    }

    @Test
    void findAll_ShouldReturnAllProducts() {
        // Arrange
        List<Product> products = Arrays.asList(testProduct, new Product());
        when(productRepository.findAll()).thenReturn(products);

        // Act
        List<Product> result = productService.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(productRepository).findAll();
    }

    @Test
    void updateProduct_ShouldUpdateExistingProduct() {
        // Arrange
        Product updates = new Product();
        updates.setNombre("Updated Product");
        updates.setCantidad(15);

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        Product result = productService.updateProduct(1L, updates);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Product", result.getNombre());
        assertEquals(15, result.getCantidad());
        verify(productRepository).findById(1L);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void deleteProduct_ShouldDeleteWhenExists() {
        // Arrange
        when(productRepository.existsById(1L)).thenReturn(true);
        doNothing().when(productRepository).deleteById(1L);

        // Act
        productService.deleteProduct(1L);

        // Assert
        verify(productRepository).existsById(1L);
        verify(productRepository).deleteById(1L);
    }

    @Test
    void deleteProduct_ShouldThrowExceptionWhenNotExists() {
        // Arrange
        when(productRepository.existsById(1L)).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> productService.deleteProduct(1L));
        verify(productRepository).existsById(1L);
        verify(productRepository, never()).deleteById(any());
    }

    @Test
    void findByNombre_ShouldReturnMatchingProducts() {
        // Arrange
        String searchTerm = "Test";
        when(productRepository.findByNombreContainingIgnoreCase(searchTerm)).thenReturn(List.of(testProduct));

        // Act
        List<Product> result = productService.findByNombre(searchTerm);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Product", result.get(0).getNombre());
        verify(productRepository).findByNombreContainingIgnoreCase(searchTerm);
    }

    @Test
    void findByCategoria_ShouldReturnCategoryProducts() {
        // Arrange
        Categoria categoria = Categoria.ELECTRONICA;
        when(productRepository.findByCategoria(categoria)).thenReturn(List.of(testProduct));

        // Act
        List<Product> result = productService.findByCategoria(categoria);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Categoria.ELECTRONICA, result.get(0).getCategoria());
        verify(productRepository).findByCategoria(categoria);
    }

    @Test
    void findByPrecioRange_ShouldReturnProductsInRange() {
        // Arrange
        Double min = 900.0;
        Double max = 1000.0;
        when(productRepository.findByPrecioBetween(min, max)).thenReturn(List.of(testProduct));

        // Act
        List<Product> result = productService.findByPrecioRange(min, max);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(999.99, result.get(0).getPrecio());
        verify(productRepository).findByPrecioBetween(min, max);
    }

    @Test
    void verificarDisponibilidad_ShouldReturnTrueWhenEnoughStock() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // Act
        boolean result = productService.verificarDisponibilidad(1L, 5);

        // Assert
        assertTrue(result);
        verify(productRepository).findById(1L);
    }

    @Test
    void verificarDisponibilidad_ShouldReturnFalseWhenNotEnoughStock() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // Act
        boolean result = productService.verificarDisponibilidad(1L, 15);

        // Assert
        assertFalse(result);
        verify(productRepository).findById(1L);
    }

    @Test
    void actualizarStock_ShouldUpdateStock() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        productService.actualizarStock(1L, 15);

        // Assert
        assertEquals(15, testProduct.getCantidad());
        verify(productRepository).save(testProduct);
    }

    @Test
    void revertirStock_ShouldAddToStock() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        productService.revertirStock(1L, 5);

        // Assert
        assertEquals(15, testProduct.getCantidad());
        verify(productRepository).save(testProduct);
    }
}