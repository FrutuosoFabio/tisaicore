package br.com.tisaicore.controller;

import br.com.tisaicore.dto.request.ClientRegistrationRequest;
import br.com.tisaicore.dto.response.ClientRegistrationResponse;
import br.com.tisaicore.service.ClientRegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/register")
public class ClientRegistrationController {

    private final ClientRegistrationService clientRegistrationService;

    public ClientRegistrationController(ClientRegistrationService clientRegistrationService) {
        this.clientRegistrationService = clientRegistrationService;
    }

    @PostMapping
    public ResponseEntity<ClientRegistrationResponse> register(
            @Valid @RequestBody ClientRegistrationRequest request) {
        ClientRegistrationResponse response = clientRegistrationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
