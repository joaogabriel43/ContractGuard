package br.com.contractguard.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterServiceRequest(
        @NotBlank(message = "Service name must not be blank")
        String name,

        @NotBlank(message = "Service slug must not be blank")
        @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug must contain only lowercase letters, numbers, and hyphens")
        String slug
) {
}
