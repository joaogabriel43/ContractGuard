package br.com.contractguard.infrastructure.persistence.repository;

import br.com.contractguard.infrastructure.persistence.entity.ApiSpecificationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApiSpecificationJpaRepository extends JpaRepository<ApiSpecificationJpaEntity, UUID> {
    
    Optional<ApiSpecificationJpaEntity> findFirstByServiceIdOrderByCreatedAtDesc(UUID serviceId);
    
    boolean existsByServiceIdAndVersion(UUID serviceId, String version);
}
