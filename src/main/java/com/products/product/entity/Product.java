package com.products.product.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    private Long id;

    private String nombre;

    @Enumerated(EnumType.STRING)
    private Categoria categoria;

    private Double precio;
    private Integer cantidad;
    private String descripcion;
}