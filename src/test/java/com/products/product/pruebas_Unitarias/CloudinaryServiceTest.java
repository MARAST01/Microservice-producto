package com.products.product.pruebas_Unitarias;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.cloudinary.utils.ObjectUtils;
import com.products.product.service.CloudinaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CloudinaryServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @InjectMocks
    private CloudinaryService cloudinaryService;

    private MultipartFile testFile;

    @BeforeEach
    void setUp() {
        testFile = new MockMultipartFile("test.jpg", "test.jpg", "image/jpeg", "test image content".getBytes());
        when(cloudinary.uploader()).thenReturn(uploader);
    }

    @Test
    void uploadImage_ShouldReturnImageUrl() throws IOException {
        // Arrange
        String expectedUrl = "http://cloudinary.com/test.jpg";
        when(uploader.upload(any(byte[].class), eq(ObjectUtils.emptyMap())))
                .thenReturn(Map.of("url", expectedUrl));

        // Act
        String result = cloudinaryService.uploadImage(testFile);

        // Assert
        assertEquals(expectedUrl, result);
        verify(uploader).upload(any(byte[].class), eq(ObjectUtils.emptyMap()));
    }

    @Test
    void uploadImage_ShouldThrowExceptionWhenUploadFails() throws IOException {
        // Arrange
        when(uploader.upload(any(byte[].class), eq(ObjectUtils.emptyMap())))
                .thenThrow(new IOException("Upload failed"));

        // Act & Assert
        assertThrows(IOException.class, () -> cloudinaryService.uploadImage(testFile));
        verify(uploader).upload(any(byte[].class), eq(ObjectUtils.emptyMap()));
    }

    @Test
    void deleteImage_ShouldDeleteImage() throws IOException {
        // Arrange
        String imageUrl = "http://cloudinary.com/test.jpg";
        Map response = Map.of("result", "ok");
        when(uploader.destroy(eq("test"), eq(ObjectUtils.emptyMap())))
                .thenReturn(response);

        // Act
        cloudinaryService.deleteImage(imageUrl);

        // Assert
        verify(uploader).destroy(eq("test"), eq(ObjectUtils.emptyMap()));
    }

    @Test
    void deleteImage_ShouldThrowExceptionWhenDeleteFails() throws IOException {
        // Arrange
        String imageUrl = "http://cloudinary.com/test.jpg";
        when(uploader.destroy(eq("test"), eq(ObjectUtils.emptyMap())))
                .thenThrow(new IOException("Delete failed"));

        // Act & Assert
        assertThrows(IOException.class, () -> cloudinaryService.deleteImage(imageUrl));
        verify(uploader).destroy(eq("test"), eq(ObjectUtils.emptyMap()));
    }
}