package br.com.tisaicore.dto.request;

import java.time.LocalDate;

public record UpdateBatchRequest(
        LocalDate expirationDate,
        LocalDate manufacturingDate,
        String supplier,
        String notes
) {}
