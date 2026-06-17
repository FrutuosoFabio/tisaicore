package br.com.tisaicore.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Loga path + subject + authorities + scope sempre que uma request toma 403
 * pelo filter chain do Spring Security. Sem esse handler customizado, o
 * AuthorizationFilter rejeita silenciosamente (nem o @RestControllerAdvice
 * com @ExceptionHandler(AccessDeniedException.class) é chamado).
 */
@Component
public class LoggingAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger log = LoggerFactory.getLogger(LoggingAccessDeniedHandler.class);

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String subject = "<anon>";
        String authorities = "<none>";
        Object scope = null;
        Object userId = null;
        if (auth != null) {
            subject = auth.getName();
            authorities = auth.getAuthorities().stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(","));
            if (auth.getPrincipal() instanceof Jwt jwt) {
                scope = jwt.getClaim("scope");
                userId = jwt.getClaim("userId");
            }
        }
        log.warn("[ACCESS-DENIED] {} {} subject={} userId={} authorities=[{}] scope={} reason={}",
                request.getMethod(), request.getRequestURI(),
                subject, userId, authorities, scope, ex.getMessage());

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        String body = String.format(
                "{\"timestamp\":\"%s\",\"status\":403,\"error\":\"Forbidden\","
                        + "\"message\":\"Você não tem permissão para realizar essa ação.\"}",
                LocalDateTime.now());
        response.getWriter().write(body);
    }
}
