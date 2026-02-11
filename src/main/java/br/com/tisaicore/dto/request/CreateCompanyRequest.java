package br.com.tisaicore.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateCompanyRequest(
        @NotBlank(message = "Trade name is required")
        String tradeName,

        @NotBlank(message = "Legal name is required")
        String legalName,

        @NotBlank(message = "CNPJ is required")
        String cnpj,

        String email,

        String phone
) {}
