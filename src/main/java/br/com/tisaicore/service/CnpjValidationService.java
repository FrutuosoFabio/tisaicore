package br.com.tisaicore.service;

import br.com.tisaicore.dto.response.CnpjResponse;
import br.com.tisaicore.exception.CnpjValidationException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
public class CnpjValidationService {

    private static final String BRASIL_API_URL = "https://brasilapi.com.br/api/cnpj/v1/{cnpj}";

    private final RestClient restClient;

    public CnpjValidationService() {
        this.restClient = RestClient.create();
    }

    public CnpjResponse validate(String cnpj) {
        String sanitizedCnpj = cnpj.replaceAll("[^0-9]", "");

        if (sanitizedCnpj.length() != 14) {
            throw new CnpjValidationException("Formato de CNPJ inválido: deve conter 14 dígitos");
        }

        CnpjResponse response;
        try {
            response = restClient.get()
                    .uri(BRASIL_API_URL, sanitizedCnpj)
                    .retrieve()
                    .body(CnpjResponse.class);
        } catch (RestClientResponseException ex) {
            throw new CnpjValidationException("CNPJ não encontrado: " + sanitizedCnpj);
        }

        if (response == null) {
            throw new CnpjValidationException("CNPJ não encontrado: " + sanitizedCnpj);
        }

        if (!"ATIVA".equalsIgnoreCase(response.descricaoSituacaoCadastral())) {
            throw new CnpjValidationException(
                    "CNPJ não está ativo. Situação atual: " + response.descricaoSituacaoCadastral()
            );
        }

        return response;
    }
}
