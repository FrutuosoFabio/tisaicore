package br.com.tisaicore.dto.response;

import br.com.tisaicore.entity.Company;

import java.time.LocalDateTime;

public record CompanyResponse(
        Long id,
        String tradeName,
        String legalName,
        String cnpj,
        String email,
        String phone,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CompanyResponse from(Company company) {
        return new CompanyResponse(
                company.getId(),
                company.getTradeName(),
                company.getLegalName(),
                company.getCnpj(),
                company.getEmail(),
                company.getPhone(),
                company.isActive(),
                company.getCreatedAt(),
                company.getUpdatedAt()
        );
    }
}
