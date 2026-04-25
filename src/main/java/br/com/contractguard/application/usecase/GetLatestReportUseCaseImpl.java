package br.com.contractguard.application.usecase;

import br.com.contractguard.domain.exception.DomainException;
import br.com.contractguard.domain.model.analyzer.DiffReport;
import br.com.contractguard.domain.model.catalog.Service;
import br.com.contractguard.domain.port.in.GetLatestReportUseCase;
import br.com.contractguard.domain.port.out.DiffReportRepositoryPort;
import br.com.contractguard.domain.port.out.ServiceRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class GetLatestReportUseCaseImpl implements GetLatestReportUseCase {

    private final ServiceRepositoryPort serviceRepositoryPort;
    private final DiffReportRepositoryPort diffReportRepositoryPort;

    public GetLatestReportUseCaseImpl(ServiceRepositoryPort serviceRepositoryPort,
                                      DiffReportRepositoryPort diffReportRepositoryPort) {
        this.serviceRepositoryPort = serviceRepositoryPort;
        this.diffReportRepositoryPort = diffReportRepositoryPort;
    }

    @Override
    public Optional<DiffReport> execute(String slug) {
        Service service = serviceRepositoryPort.findBySlug(slug)
                .orElseThrow(() -> new DomainException("Service not found for slug: " + slug));
        
        return diffReportRepositoryPort.findLatestByServiceId(service.getId());
    }
}
