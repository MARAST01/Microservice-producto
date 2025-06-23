package com.products.product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.products.product.entity.Categoria;
import com.products.product.entity.Product;
import com.products.product.service.CloudinaryService;
import com.products.product.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private CloudinaryService cloudinaryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createProduct_shouldReturnCreatedProduct() throws Exception {
        Product product = new Product();
        product.setId(1L);
        product.setNombre("Test Product");

        MockMultipartFile productJson = new MockMultipartFile(
                "product",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsString(product).getBytes()
        );

        MockMultipartFile image = new MockMultipartFile(
                "image",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        when(cloudinaryService.uploadImage(any())).thenReturn("http://fake-url.com/image.jpg");
        when(productService.createProduct(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            p.setImagenUrl("http://fake-url.com/image.jpg");
            return p;
        });

        mockMvc.perform(multipart("/api/productos/crear")
                        .file(productJson)
                        .file(image))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("Test Product"))
                .andExpect(jsonPath("$.imagenUrl").value("http://fake-url.com/image.jpg"));
    }

    @Test
    void getProductById_shouldReturnProduct() throws Exception {
        Product product = new Product();
        product.setId(1L);
        product.setNombre("Test Product");

        when(productService.getProductById(1L)).thenReturn(product);

        mockMvc.perform(get("/api/productos/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("Test Product"));
    }

    @Test
    void findAll_shouldReturnProductList() throws Exception {
        Product product = new Product();
        product.setId(1L);
        product.setNombre("Test Product");

        when(productService.findAll()).thenReturn(Collections.singletonList(product));

        mockMvc.perform(get("/api/productos/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].nombre").value("Test Product"));
    }

    @Test
    void deleteProduct_shouldReturnOk() throws Exception {
        Product product = new Product();
        product.setId(1L);
        product.setImagenUrl("http://fake-url.com/image.jpg");

        when(productService.getProductById(1L)).thenReturn(product);
        doNothing().when(cloudinaryService).deleteImage(any());
        doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/api/productos/{id}", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void findByNombre_shouldReturnProductList() throws Exception {
        Product product = new Product();
        product.setId(1L);
        product.setNombre("Test Product");

        when(productService.findByNombre("Test Product")).thenReturn(Collections.singletonList(product));

        mockMvc.perform(get("/api/productos/buscar")
                        .param("nombre", "Test Product"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].nombre").value("Test Product"));
    }

    @Test
    void findByCategoria_shouldReturnProductList() throws Exception {
        Product product = new Product();
        product.setId(1L);
        product.setCategoria(Categoria.ELECTRONICA);

        when(productService.findByCategoria(Categoria.ELECTRONICA)).thenReturn(Collections.singletonList(product));

        mockMvc.perform(get("/api/productos/categoria/{categoria}", Categoria.ELECTRONICA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].categoria").value("ELECTRONICA"));
    }

    @Test
    void findByPrecioRange_shouldReturnProductList() throws Exception {
        Product product = new Product();
        product.setId(1L);
        product.setPrecio(50.0);

        when(productService.findByPrecioRange(10.0, 100.0)).thenReturn(Collections.singletonList(product));

        mockMvc.perform(get("/api/productos/precio")
                        .param("precioMin", "10.0")
                        .param("precioMax", "100.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].precio").value(50.0));
    }

    @Test
    void verificarDisponibilidad_shouldReturnAvailability() throws Exception {
        Product product = new Product();
        product.setCantidad(10);

        when(productService.verificarDisponibilidad(1L, 5)).thenReturn(true);
        when(productService.getProductById(1L)).thenReturn(product);

        mockMvc.perform(get("/api/productos/{id}/disponibilidad", 1L)
                        .param("cantidad", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disponible").value(true))
                .andExpect(jsonPath("$.stockActual").value(10));
    }

    @Test
    void actualizarStock_shouldReturnUpdatedStock() throws Exception {
        Product product = new Product();
        product.setCantidad(5);

        doNothing().when(productService).actualizarStock(1L, 5);
        when(productService.getProductById(1L)).thenReturn(product);

        mockMvc.perform(post("/api/productos/{id}/stock", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cantidad\": 5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockActual").value(5));
    }

    @Test
    void revertirStock_shouldReturnRevertedStock() throws Exception {
        Product product = new Product();
        product.setCantidad(15);

        doNothing().when(productService).revertirStock(1L, 5);
        when(productService.getProductById(1L)).thenReturn(product);

        mockMvc.perform(post("/api/productos/{id}/revertir-stock", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cantidad\": 5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockActual").value(15));
    }
} 