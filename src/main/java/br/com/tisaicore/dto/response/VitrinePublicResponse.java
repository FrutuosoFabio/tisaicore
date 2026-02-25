package br.com.tisaicore.dto.response;

import br.com.tisaicore.entity.Product;

public record VitrinePublicResponse(
        Long id,
        String name,
        String description,
        String sku,
        Long brandId,
        String brandName,
        Long categoryId,
        String categoryName,
        boolean active,
        String imageUrl
) {
    public static VitrinePublicResponse from(Product product, String imageUrl) {
        return new VitrinePublicResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getSku(),
                product.getBrand() != null ? product.getBrand().getId() : null,
                product.getBrand() != null ? product.getBrand().getName() : null,
                product.getCategory() != null ? product.getCategory().getId() : null,
                product.getCategory() != null ? product.getCategory().getName() : null,
                product.isActive(),
                imageUrl
        );
    }
}
