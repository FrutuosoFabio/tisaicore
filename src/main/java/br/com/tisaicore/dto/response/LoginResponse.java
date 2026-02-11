package br.com.tisaicore.dto.response;

public record LoginResponse(
        String accessToken,
        Long expiresIn,
        UserResponse user
) {}
