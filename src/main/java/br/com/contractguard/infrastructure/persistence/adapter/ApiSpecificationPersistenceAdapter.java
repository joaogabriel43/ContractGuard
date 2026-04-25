package br.com.contractguard.infrastructure.persistence.adapter;

import br.com.contractguard.domain.model.catalog.ApiSpecification;
import br.com.contractguard.domain.port.out.ApiSpecificationRepositoryPort;
import br.com.contractguard.infrastructure.persistence.entity.ApiSpecificationJpaEntity;
import br.com.contractguard.infrastructure.persistence.repository.ApiSpecificationJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class ApiSpecificationPersistenceAdapter implements ApiSpecificationRepositoryPort {

    private final ApiSpecificationJpaRepository repository;

    public ApiSpecificationPersistenceAdapter(ApiSpecificationJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public ApiSpecification save(ApiSpecification specification) {
        ApiSpecificationJpaEntity entity = ApiSpecificationJpaEntity.builder()
                .id(specification.getId())
                .serviceId(specification.getServiceId())
                .version(specification.getVersion())
                .rawContent(specification.getRawContent())
                .createdAt(specification.getCreatedAt())
                .build();

        ApiSpecificationJpaEntity saved = repository.save(entity);
        return mapToDomain(saved);
    }

    @Override
    public Optional<ApiSpecification> findById(UUID id) {
        return repository.findById(id).map(this::mapToDomain);
    }

    @Override
    public Optional<ApiSpecification> findLatestByServiceId(UUID serviceId) {
        return repository.findFirstByServiceIdOrderByCreatedAtDesc(serviceId).map(this::mapToDomain);
    }

    @Override
    public boolean existsByServiceIdAndVersion(UUID serviceId, String version) {
        return repository.existsByServiceIdAndVersion(serviceId, version);
    }

    private ApiSpecification mapToDomain(ApiSpecificationJpaEntity entity) {
        return ApiSpecification.reconstitute(
                entity.getId(),
                entity.getServiceId(),
                entity.getRawContent(),
                entity.getVersion(),
                entity.getCreatedAt()
        );
    }
}
