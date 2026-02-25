package br.com.tisaicore.dto.response;

import br.com.tisaicore.entity.Product;

import java.math.BigDecimal;

public record VitrineAdminResponse(
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
        String imageUrl
) {
    public static VitrineAdminResponse from(Product product, String imageUrl) {
        return new VitrineAdminResponse(
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
                imageUrl
        );
    }
}
