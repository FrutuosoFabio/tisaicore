package br.com.tisaicore.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequest(
        @NotBlank(message = "Nome é obrigatório")
        String name,

        String description
) {}
