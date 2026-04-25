package br.com.contractguard.presentation.response;

public record ViolationResponse(
        String path,
        String httpMethod,
        String ruleType,
        String severity,
        String message
) {
}
