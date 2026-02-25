package br.com.tisaicore.dto.response;

import br.com.tisaicore.entity.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        String name,
        String description,
        String sku,
        BigDecimal price,
        Integer stockQuantity,
        Long brandId,
        String brandName,
        Long categoryId,
        String categoryName,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String imageUrl
) {
    public static ProductResponse from(Product product) {
        return from(product, null);
    }

    public static ProductResponse from(Product product, String imageUrl) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getSku(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getBrand() != null ? product.getBrand().getId() : null,
                product.getBrand() != null ? product.getBrand().getName() : null,
                product.getCategory() != null ? product.getCategory().getId() : null,
                product.getCategory() != null ? product.getCategory().getName() : null,
                product.isActive(),
                product.getCreatedAt(),
                product.getUpdatedAt(),
                imageUrl
        );
    }
}
