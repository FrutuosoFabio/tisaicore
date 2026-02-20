package br.com.tisaicore.dto.request;

import jakarta.validation.constraints.NotBlank;

public record BrandRequest(
        @NotBlank(message = "Name is required")
        String name,

        String description
) {}
