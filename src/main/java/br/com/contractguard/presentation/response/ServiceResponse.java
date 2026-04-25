package br.com.contractguard.presentation.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ServiceResponse(
        UUID id,
        String name,
        String slug,
        LocalDateTime createdAt
) {
}
