package br.com.contractguard.domain.model.analyzer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root representing the result of comparing two {@code ApiSpecification} versions.
 */
@Getter
@ToString
@EqualsAndHashCode(of = "id")
public class DiffReport {

    private final UUID id;
    private final UUID serviceId;
    private final UUID baseApiSpecificationId; // nullable if first spec
    private final UUID candidateApiSpecificationId;
    private final String baseSpecVersion; // nullable if first spec
    private final String candidateSpecVersion;
    private final List<Violation> violations;
    private final LocalDateTime generatedAt;

    private DiffReport(UUID id, UUID serviceId, UUID baseApiSpecificationId,
                       UUID candidateApiSpecificationId, String baseSpecVersion,
                       String candidateSpecVersion, List<Violation> violations,
                       LocalDateTime generatedAt) {
        Objects.requireNonNull(serviceId, "DiffReport serviceId must not be null");
        Objects.requireNonNull(candidateApiSpecificationId, "DiffReport candidateApiSpecificationId must not be null");
        Objects.requireNonNull(candidateSpecVersion, "DiffReport candidateSpecVersion must not be null");
        Objects.requireNonNull(violations, "DiffReport violations list must not be null");

        this.id = (id != null) ? id : UUID.randomUUID();
        this.serviceId = serviceId;
        this.baseApiSpecificationId = baseApiSpecificationId;
        this.candidateApiSpecificationId = candidateApiSpecificationId;
        this.baseSpecVersion = baseSpecVersion;
        this.candidateSpecVersion = candidateSpecVersion;
        this.violations = Collections.unmodifiableList(new ArrayList<>(violations));
        this.generatedAt = (generatedAt != null) ? generatedAt : LocalDateTime.now();
    }

    public static DiffReport create(UUID serviceId, UUID baseApiSpecificationId,
                                    UUID candidateApiSpecificationId, String baseSpecVersion,
                                    String candidateSpecVersion, List<Violation> violations) {
        return new DiffReport(UUID.randomUUID(), serviceId, baseApiSpecificationId,
                candidateApiSpecificationId, baseSpecVersion, candidateSpecVersion,
                violations, LocalDateTime.now());
    }
    
    public static DiffReport createWithoutBaseline(UUID serviceId, UUID candidateApiSpecificationId, String candidateSpecVersion) {
        return new DiffReport(UUID.randomUUID(), serviceId, null, candidateApiSpecificationId,
                null, candidateSpecVersion, Collections.emptyList(), LocalDateTime.now());
    }

    public static DiffReport reconstitute(UUID id, UUID serviceId, UUID baseApiSpecificationId,
                                          UUID candidateApiSpecificationId, String baseSpecVersion,
                                          String candidateSpecVersion, List<Violation> violations,
                                          LocalDateTime generatedAt) {
        return new DiffReport(id, serviceId, baseApiSpecificationId, candidateApiSpecificationId,
                baseSpecVersion, candidateSpecVersion, violations, generatedAt);
    }

    public boolean hasBreakingChanges() {
        return violations.stream().anyMatch(Violation::isBreaking);
    }

    public boolean isCompatible() {
        return !hasBreakingChanges();
    }

    public List<Violation> getBreakingViolations() {
        return violations.stream()
                .filter(Violation::isBreaking)
                .toList();
    }

    public List<Violation> getViolationsBySeverity(ViolationSeverity severity) {
        return violations.stream()
                .filter(v -> v.severity() == severity)
                .toList();
    }

    public int getViolationCount() {
        return violations.size();
    }
}
