package br.com.tisaicore.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CnpjResponse(
        String cnpj,

        @JsonProperty("razao_social")
        String razaoSocial,

        @JsonProperty("nome_fantasia")
        String nomeFantasia,

        @JsonProperty("descricao_situacao_cadastral")
        String descricaoSituacaoCadastral
) {}
