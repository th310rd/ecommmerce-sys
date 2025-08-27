package org.example.productservice.dto;

import org.example.productservice.model.Product;
import org.example.productservice.model.Rating;

import java.math.BigDecimal;

public class ProductMapper {

    public static Product toEntity(ProductRequestDTO dto) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStockQuantity(dto.getStockQuantity());
        product.setCategory(dto.getCategory());
        product.setImageUrl(dto.getImageUrl());

        if (dto.getRating() != null) {
            product.setRating(Rating.valueOf(dto.getRating().toUpperCase()));
        }

        return product;
    }

    public static ProductResponseDTO toResponseDTO(Product product) {
        return new ProductResponseDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getCategory(),
                product.getImageUrl(),
                product.getRating() != null ? product.getRating().name() : null
        );
    }
}
