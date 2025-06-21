package com.products.product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.products.product.entity.Product;
import com.products.product.service.CloudinaryService;
import com.products.product.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
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

        mockMvc.perform(multipart("/api/product/crear")
                        .file(productJson)
                        .file(image))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("Test Product"))
                .andExpect(jsonPath("$.imagenUrl").value("http://fake-url.com/image.jpg"));
    }
} 