package br.com.tisaicore.dto.response;

import br.com.tisaicore.entity.ProductImage;

import java.time.LocalDateTime;

public record ProductImageResponse(
        Long id,
        Long sisFileId,
        String originalName,
        String contentType,
        Long size,
        String url,
        Integer displayOrder,
        LocalDateTime createdAt
) {
    public static ProductImageResponse from(ProductImage image, String url) {
        return new ProductImageResponse(
                image.getId(),
                image.getSisFile().getId(),
                image.getSisFile().getOriginalName(),
                image.getSisFile().getContentType(),
                image.getSisFile().getSize(),
                url,
                image.getDisplayOrder(),
                image.getCreatedAt()
        );
    }
}
