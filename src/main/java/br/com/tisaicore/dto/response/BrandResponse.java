package br.com.tisaicore.dto.response;

import br.com.tisaicore.entity.Brand;

import java.time.LocalDateTime;

public record BrandResponse(
        Long id,
        String name,
        String description,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static BrandResponse from(Brand brand) {
        return new BrandResponse(
                brand.getId(),
                brand.getName(),
                brand.getDescription(),
                brand.isActive(),
                brand.getCreatedAt(),
                brand.getUpdatedAt()
        );
    }
}
