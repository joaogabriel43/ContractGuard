package br.com.contractguard.presentation.controller;

import br.com.contractguard.domain.model.analyzer.DiffReport;
import br.com.contractguard.domain.port.in.AnalyzeContractUseCase;
import br.com.contractguard.presentation.mapper.DiffReportMapper;
import br.com.contractguard.presentation.request.AnalyzeContractRequest;
import br.com.contractguard.presentation.response.DiffReportResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/services")
public class AnalyzerController {

    private final AnalyzeContractUseCase analyzeContractUseCase;
    private final br.com.contractguard.domain.port.in.GetLatestReportUseCase getLatestReportUseCase;

    public AnalyzerController(AnalyzeContractUseCase analyzeContractUseCase,
                              br.com.contractguard.domain.port.in.GetLatestReportUseCase getLatestReportUseCase) {
        this.analyzeContractUseCase = analyzeContractUseCase;
        this.getLatestReportUseCase = getLatestReportUseCase;
    }

    @PostMapping("/{slug}/analyze")
    public ResponseEntity<DiffReportResponse> analyze(
            @PathVariable String slug,
            @Valid @RequestBody AnalyzeContractRequest request) {

        DiffReport report = analyzeContractUseCase.analyze(slug, request.rawContent(), request.version());
        return ResponseEntity.ok(DiffReportMapper.toResponse(report));
    }

    @org.springframework.web.bind.annotation.GetMapping("/{slug}/reports/latest")
    public ResponseEntity<DiffReportResponse> getLatestReport(@PathVariable String slug) {
        return getLatestReportUseCase.execute(slug)
                .map(report -> ResponseEntity.ok(DiffReportMapper.toResponse(report)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}
