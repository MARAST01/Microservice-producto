package com.products.product.pruebas_Unitarias;

import com.products.product.config.CorsConfig;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.mockito.Mockito.*;

class CorsConfigTest {

    @Test
    void corsConfigurer_ShouldConfigureCorsCorrectly() {
        // Arrange
        CorsConfig corsConfig = new CorsConfig();
        CorsRegistry registry = mock(CorsRegistry.class);
        CorsRegistration corsRegistration = mock(CorsRegistration.class);

        // Configure the mocks to return themselves for method chaining
        when(registry.addMapping("/**")).thenReturn(corsRegistration);
        when(corsRegistration.allowedOrigins("http://localhost:5173")).thenReturn(corsRegistration);
        when(corsRegistration.allowedMethods("GET", "POST", "PUT", "DELETE")).thenReturn(corsRegistration);
        when(corsRegistration.allowedHeaders("*")).thenReturn(corsRegistration);

        // Act
        WebMvcConfigurer configurer = corsConfig.corsConfigurer();
        configurer.addCorsMappings(registry);

        // Assert
        verify(registry).addMapping("/**");
        verify(corsRegistration).allowedOrigins("http://localhost:5173");
        verify(corsRegistration).allowedMethods("GET", "POST", "PUT", "DELETE");
        verify(corsRegistration).allowedHeaders("*");
    }
}