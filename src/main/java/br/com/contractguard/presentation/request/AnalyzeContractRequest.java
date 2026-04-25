package br.com.contractguard.presentation.request;

import jakarta.validation.constraints.NotBlank;

public record AnalyzeContractRequest(
        @NotBlank(message = "Version must not be blank")
        String version,

        @NotBlank(message = "Raw content must not be blank")
        String rawContent
) {
}
