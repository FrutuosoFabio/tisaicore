package br.com.tisaicore.dto.response;

public record ClientRegistrationResponse(
        UserResponse user,
        CompanyResponse company
) {}
