package br.com.contractguard.application.usecase;

import br.com.contractguard.domain.exception.DomainException;
import br.com.contractguard.domain.model.analyzer.DiffReport;
import br.com.contractguard.domain.model.catalog.ApiSpecification;
import br.com.contractguard.domain.model.catalog.Service;
import br.com.contractguard.domain.port.in.AnalyzeContractUseCase;
import br.com.contractguard.domain.port.in.UploadSpecificationUseCase;
import br.com.contractguard.domain.port.out.ApiSpecificationRepositoryPort;
import br.com.contractguard.domain.port.out.ContractAnalyzerPort;
import br.com.contractguard.domain.port.out.DiffReportRepositoryPort;
import br.com.contractguard.domain.port.out.ServiceRepositoryPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class AnalyzeContractUseCaseImpl implements AnalyzeContractUseCase {

    private final ServiceRepositoryPort serviceRepositoryPort;
    private final ApiSpecificationRepositoryPort apiSpecificationRepositoryPort;
    private final UploadSpecificationUseCase uploadSpecificationUseCase;
    private final ContractAnalyzerPort contractAnalyzerPort;
    private final DiffReportRepositoryPort diffReportRepositoryPort;

    public AnalyzeContractUseCaseImpl(ServiceRepositoryPort serviceRepositoryPort,
                                      ApiSpecificationRepositoryPort apiSpecificationRepositoryPort,
                                      UploadSpecificationUseCase uploadSpecificationUseCase,
                                      ContractAnalyzerPort contractAnalyzerPort,
                                      DiffReportRepositoryPort diffReportRepositoryPort) {
        this.serviceRepositoryPort = serviceRepositoryPort;
        this.apiSpecificationRepositoryPort = apiSpecificationRepositoryPort;
        this.uploadSpecificationUseCase = uploadSpecificationUseCase;
        this.contractAnalyzerPort = contractAnalyzerPort;
        this.diffReportRepositoryPort = diffReportRepositoryPort;
    }

    @Override
    @Transactional
    public DiffReport analyze(String serviceSlug, String newSpecContent, String version) {
        // 1. Find service
        Service service = serviceRepositoryPort.findBySlug(serviceSlug)
                .orElseThrow(() -> new DomainException(String.format("Service with slug '%s' not found", serviceSlug)));

        // 2. Find baseline before saving the new one
        Optional<ApiSpecification> baselineOpt = apiSpecificationRepositoryPort.findLatestByServiceId(service.getId());

        // 3. Upload new candidate spec
        ApiSpecification candidateSpec = uploadSpecificationUseCase.upload(service.getId(), newSpecContent, version);

        // 4. Generate report
        DiffReport report;
        if (baselineOpt.isPresent()) {
            ApiSpecification baseline = baselineOpt.get();
            report = contractAnalyzerPort.analyze(
                    service.getId(),
                    baseline.getId(),
                    candidateSpec.getId(),
                    baseline.getRawContent(),
                    candidateSpec.getRawContent()
            );
        } else {
            // No baseline exists, it's the first spec. Return an empty compatible report.
            report = DiffReport.createWithoutBaseline(service.getId(), candidateSpec.getId(), candidateSpec.getVersion());
        }

        // 5. Save and return report
        return diffReportRepositoryPort.save(report);
    }
}
