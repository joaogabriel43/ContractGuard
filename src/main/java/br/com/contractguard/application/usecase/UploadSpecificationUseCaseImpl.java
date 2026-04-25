package br.com.contractguard.application.usecase;

import br.com.contractguard.domain.exception.DomainException;
import br.com.contractguard.domain.model.catalog.ApiSpecification;
import br.com.contractguard.domain.port.in.UploadSpecificationUseCase;
import br.com.contractguard.domain.port.out.ApiSpecificationRepositoryPort;
import br.com.contractguard.domain.port.out.ServiceRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UploadSpecificationUseCaseImpl implements UploadSpecificationUseCase {

    private final ServiceRepositoryPort serviceRepositoryPort;
    private final ApiSpecificationRepositoryPort apiSpecificationRepositoryPort;

    public UploadSpecificationUseCaseImpl(ServiceRepositoryPort serviceRepositoryPort,
                                          ApiSpecificationRepositoryPort apiSpecificationRepositoryPort) {
        this.serviceRepositoryPort = serviceRepositoryPort;
        this.apiSpecificationRepositoryPort = apiSpecificationRepositoryPort;
    }

    @Override
    public ApiSpecification upload(UUID serviceId, String rawContent, String version) {
        serviceRepositoryPort.findById(serviceId)
                .orElseThrow(() -> new DomainException(String.format("Service with ID '%s' not found", serviceId)));

        if (apiSpecificationRepositoryPort.existsByServiceIdAndVersion(serviceId, version)) {
            throw new DomainException(String.format("Version '%s' already exists for service '%s'", version, serviceId));
        }

        ApiSpecification spec = ApiSpecification.create(serviceId, rawContent, version);
        return apiSpecificationRepositoryPort.save(spec);
    }
}
