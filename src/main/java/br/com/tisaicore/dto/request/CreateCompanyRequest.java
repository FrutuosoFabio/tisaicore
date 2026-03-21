package br.com.tisaicore.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateCompanyRequest(
        @NotBlank(message = "Nome fantasia é obrigatório")
        String tradeName,

        @NotBlank(message = "Razão social é obrigatória")
        String legalName,

        @NotBlank(message = "CNPJ é obrigatório")
        String cnpj,

        String email,

        String phone
) {}
