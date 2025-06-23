package com.products.product.pruebas_Unitarias;


import com.cloudinary.Cloudinary;
import com.products.product.config.CloudinaryConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "cloudinary.cloud_name=test_cloud",
        "cloudinary.api_key=test_key",
        "cloudinary.api_secret=test_secret"
})
class CloudinaryConfigTest {

    @Autowired
    private CloudinaryConfig cloudinaryConfig;

    @Test
    void cloudinaryBean_ShouldBeCreatedWithCorrectConfig() {
        // Act
        Cloudinary cloudinary = cloudinaryConfig.cloudinary();

        // Assert
        assertNotNull(cloudinary);
        assertEquals("test_cloud", cloudinary.config.cloudName);
        assertEquals("test_key", cloudinary.config.apiKey);
        assertEquals("test_secret", cloudinary.config.apiSecret);
    }
}