package br.com.tisaicore.dto.request;

import jakarta.validation.constraints.NotBlank;

public record BrandRequest(
        @NotBlank(message = "Nome é obrigatório")
        String name,

        String description
) {}
