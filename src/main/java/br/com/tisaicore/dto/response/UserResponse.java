package br.com.tisaicore.dto.response;

import br.com.tisaicore.entity.Role;
import br.com.tisaicore.entity.User;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String name,
        String email,
        Role role,
        boolean active,
        boolean emailActive,
        String companyName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.isActive(),
                user.isEmailActive(),
                user.getCompany() != null ? user.getCompany().getTradeName() : null,
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
