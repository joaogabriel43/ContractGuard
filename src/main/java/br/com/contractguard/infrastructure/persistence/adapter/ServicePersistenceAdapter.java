package br.com.contractguard.infrastructure.persistence.adapter;

import br.com.contractguard.domain.model.catalog.Service;
import br.com.contractguard.domain.port.out.ServiceRepositoryPort;
import br.com.contractguard.infrastructure.persistence.entity.ServiceJpaEntity;
import br.com.contractguard.infrastructure.persistence.repository.ServiceJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ServicePersistenceAdapter implements ServiceRepositoryPort {

    private final ServiceJpaRepository repository;

    public ServicePersistenceAdapter(ServiceJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Service save(Service service) {
        ServiceJpaEntity entity = ServiceJpaEntity.builder()
                .id(service.getId())
                .name(service.getName())
                .slug(service.getSlug())
                .createdAt(service.getCreatedAt())
                .build();
        
        ServiceJpaEntity saved = repository.save(entity);
        return mapToDomain(saved);
    }

    @Override
    public Optional<Service> findById(UUID id) {
        return repository.findById(id).map(this::mapToDomain);
    }

    @Override
    public Optional<Service> findBySlug(String slug) {
        return repository.findBySlug(slug).map(this::mapToDomain);
    }

    @Override
    public boolean existsBySlug(String slug) {
        return repository.existsBySlug(slug);
    }

    @Override
    public List<Service> findAll() {
        return repository.findAll().stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    private Service mapToDomain(ServiceJpaEntity entity) {
        return Service.reconstitute(
                entity.getId(),
                entity.getName(),
                entity.getSlug(),
                entity.getCreatedAt()
        );
    }
}
