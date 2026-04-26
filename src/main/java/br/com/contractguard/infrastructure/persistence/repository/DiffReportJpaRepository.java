package br.com.contractguard.infrastructure.persistence.repository;

import br.com.contractguard.infrastructure.persistence.entity.DiffReportJpaEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DiffReportJpaRepository extends JpaRepository<DiffReportJpaEntity, UUID> {

    @EntityGraph(attributePaths = {"violations"})
    Optional<DiffReportJpaEntity> findFirstByServiceIdOrderByCreatedAtDesc(UUID serviceId);
}
