package br.com.contractguard.presentation.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record DiffReportResponse(
        UUID id,
        UUID serviceId,
        String baseSpecVersion,
        String candidateSpecVersion,
        boolean hasBreakingChanges,
        int violationCount,
        List<ViolationResponse> violations,
        LocalDateTime generatedAt
) {
}
