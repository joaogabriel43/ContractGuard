package br.com.contractguard.infrastructure.persistence.adapter;

import br.com.contractguard.domain.model.analyzer.DiffReport;
import br.com.contractguard.domain.model.analyzer.Violation;
import br.com.contractguard.domain.model.analyzer.ViolationSeverity;
import br.com.contractguard.domain.model.analyzer.ViolationType;
import br.com.contractguard.domain.port.out.DiffReportRepositoryPort;
import br.com.contractguard.infrastructure.persistence.entity.DiffReportJpaEntity;
import br.com.contractguard.infrastructure.persistence.entity.ViolationJpaEntity;
import br.com.contractguard.infrastructure.persistence.repository.DiffReportJpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class DiffReportPersistenceAdapter implements DiffReportRepositoryPort {

    private final DiffReportJpaRepository repository;

    public DiffReportPersistenceAdapter(DiffReportJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public DiffReport save(DiffReport diffReport) {
        DiffReportJpaEntity entity = DiffReportJpaEntity.builder()
                .id(diffReport.getId())
                .serviceId(diffReport.getServiceId())
                .baseApiSpecificationId(diffReport.getBaseApiSpecificationId())
                .candidateApiSpecificationId(diffReport.getCandidateApiSpecificationId())
                .baseSpecVersion(diffReport.getBaseSpecVersion())
                .candidateSpecVersion(diffReport.getCandidateSpecVersion())
                .hasBreakingChanges(diffReport.hasBreakingChanges())
                .createdAt(diffReport.getGeneratedAt())
                .build();

        diffReport.getViolations().forEach(violation -> {
            ViolationJpaEntity vEntity = ViolationJpaEntity.builder()
                    .id(UUID.randomUUID())
                    .ruleType(violation.type().name())
                    .severity(violation.severity().name())
                    .path(violation.path())
                    .httpMethod(violation.httpMethod())
                    .message(violation.message())
                    .build();
            entity.addViolation(vEntity);
        });

        DiffReportJpaEntity saved = repository.save(entity);
        return mapToDomain(saved);
    }

    private DiffReport mapToDomain(DiffReportJpaEntity entity) {
        var violations = entity.getViolations().stream()
                .map(v -> Violation.of(
                        v.getPath(),
                        v.getHttpMethod(),
                        ViolationType.valueOf(v.getRuleType()),
                        ViolationSeverity.valueOf(v.getSeverity()),
                        v.getMessage()
                ))
                .collect(Collectors.toList());

        return DiffReport.reconstitute(
                entity.getId(),
                entity.getServiceId(),
                entity.getBaseApiSpecificationId(),
                entity.getCandidateApiSpecificationId(),
                entity.getBaseSpecVersion(),
                entity.getCandidateSpecVersion(),
                violations,
                entity.getCreatedAt()
        );
    }
}
