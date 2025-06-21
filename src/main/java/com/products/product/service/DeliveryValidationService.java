package com.products.product.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DeliveryValidationService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Verifica si un usuario ha recibido un producto específico
     * @param userId ID del usuario
     * @param productId ID del producto
     * @return true si el usuario ha recibido el producto (estado = "ENTREGADA"), false en caso contrario
     */
    public boolean hasUserReceivedProduct(Long userId, Long productId) {
        String sql = "SELECT COUNT(*) FROM deliveries WHERE user_id = ? AND product_id = ? AND estado = 'ENTREGADA'";
        
        try {
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, productId);
            return count != null && count > 0;
        } catch (Exception e) {
            // Si hay algún error en la consulta, asumimos que no ha recibido el producto
            return false;
        }
    }

    /**
     * Verifica si existe al menos una entrega entregada para un usuario y producto
     * @param userId ID del usuario
     * @param productId ID del producto
     * @return true si existe al menos una entrega entregada
     */
    public boolean hasAnyDeliveredProduct(Long userId, Long productId) {
        String sql = "SELECT COUNT(*) FROM deliveries WHERE user_id = ? AND product_id = ? AND estado = 'ENTREGADA'";
        
        try {
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, productId);
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
        }
    }
} 