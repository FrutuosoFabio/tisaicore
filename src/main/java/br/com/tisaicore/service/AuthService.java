package br.com.tisaicore.service;

import br.com.tisaicore.dto.request.LoginRequest;
import br.com.tisaicore.dto.response.LoginResponse;
import br.com.tisaicore.dto.response.UserResponse;
import br.com.tisaicore.entity.User;
import br.com.tisaicore.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuthService {

    // Sistema interno: token "perpétuo" (10 anos). Só termina com logout manual.
    private static final long TOKEN_EXPIRY_SECONDS = 60L * 60L * 24L * 365L * 10L;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtEncoder jwtEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("E-mail ou senha inválidos"));

        if (!user.isActive()) {
            throw new IllegalArgumentException("Conta de usuário desativada");
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("E-mail ou senha inválidos");
        }

        return buildLoginResponse(user);
    }

    /**
     * Renova o token usando o e-mail do usuário autenticado (extraído do JWT atual).
     * O token atual ainda precisa ser válido para chegar aqui.
     */
    public LoginResponse refresh(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        if (!user.isActive()) {
            throw new IllegalArgumentException("Conta de usuário desativada");
        }

        return buildLoginResponse(user);
    }

    private LoginResponse buildLoginResponse(User user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("tisaicore")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(TOKEN_EXPIRY_SECONDS))
                .subject(user.getEmail())
                .claim("scope", "ROLE_" + user.getRole().name())
                .claim("userId", user.getId())
                .build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        return new LoginResponse(token, TOKEN_EXPIRY_SECONDS, UserResponse.from(user));
    }
}
