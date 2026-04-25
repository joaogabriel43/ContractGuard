package br.com.contractguard.presentation.mapper;

import br.com.contractguard.domain.model.analyzer.DiffReport;
import br.com.contractguard.presentation.response.DiffReportResponse;
import br.com.contractguard.presentation.response.ViolationResponse;

import java.util.List;
import java.util.stream.Collectors;

public class DiffReportMapper {

    private DiffReportMapper() {
        // Utility class
    }

    public static DiffReportResponse toResponse(DiffReport diffReport) {
        if (diffReport == null) {
            return null;
        }

        List<ViolationResponse> violations = diffReport.getViolations().stream()
                .map(v -> new ViolationResponse(
                        v.path(),
                        v.httpMethod(),
                        v.type().name(),
                        v.severity().name(),
                        v.message()
                ))
                .collect(Collectors.toList());

        return new DiffReportResponse(
                diffReport.getId(),
                diffReport.getServiceId(),
                diffReport.getBaseSpecVersion(),
                diffReport.getCandidateSpecVersion(),
                diffReport.hasBreakingChanges(),
                diffReport.getViolationCount(),
                violations,
                diffReport.getGeneratedAt()
        );
    }
}
