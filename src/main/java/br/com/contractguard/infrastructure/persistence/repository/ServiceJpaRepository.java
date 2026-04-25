package br.com.contractguard.infrastructure.persistence.repository;

import br.com.contractguard.infrastructure.persistence.entity.ServiceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceJpaRepository extends JpaRepository<ServiceJpaEntity, UUID> {
    Optional<ServiceJpaEntity> findBySlug(String slug);
    boolean existsBySlug(String slug);
}
