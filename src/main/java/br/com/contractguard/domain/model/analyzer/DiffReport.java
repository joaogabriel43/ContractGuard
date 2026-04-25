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
 *
 * <p>A {@code DiffReport} is the central output of ContractGuard's analysis engine.
 * It encapsulates the complete set of {@link Violation}s detected between a base spec
 * and a candidate spec, and exposes domain-rich behaviour for CI/CD decision-making.
 *
 * <p><strong>Key business rules (verified by unit tests):</strong>
 * <ul>
 *   <li>{@link #hasBreakingChanges()} — {@code true} if any violation has severity {@code BREAKING}.</li>
 *   <li>{@link #isCompatible()} — inverse of {@link #hasBreakingChanges()}.</li>
 *   <li>{@link #getBreakingViolations()} — returns only {@code BREAKING} violations.</li>
 *   <li>{@link #getViolationsBySeverity(ViolationSeverity)} — filters violations by severity.</li>
 *   <li>{@link #getViolationCount()} — total number of violations.</li>
 * </ul>
 *
 * <p>The violations list is <strong>unmodifiable</strong> after construction to preserve invariants.
 *
 * <p><strong>No Spring, JPA, or Swagger Parser dependencies allowed in this class.</strong>
 */
@Getter
@ToString
@EqualsAndHashCode(of = "id")
public class DiffReport {

    private final UUID id;
    private final UUID serviceId;
    private final String baseSpecVersion;
    private final String candidateSpecVersion;
    private final List<Violation> violations;
    private final LocalDateTime generatedAt;

    // ──── Private constructor ─────────────────────────────────────────────────

    private DiffReport(UUID id, UUID serviceId, String baseSpecVersion,
                       String candidateSpecVersion, List<Violation> violations,
                       LocalDateTime generatedAt) {
        Objects.requireNonNull(serviceId, "DiffReport serviceId must not be null");
        Objects.requireNonNull(baseSpecVersion, "DiffReport baseSpecVersion must not be null");
        Objects.requireNonNull(candidateSpecVersion, "DiffReport candidateSpecVersion must not be null");
        Objects.requireNonNull(violations, "DiffReport violations list must not be null");

        this.id = (id != null) ? id : UUID.randomUUID();
        this.serviceId = serviceId;
        this.baseSpecVersion = baseSpecVersion;
        this.candidateSpecVersion = candidateSpecVersion;
        this.violations = Collections.unmodifiableList(new ArrayList<>(violations));
        this.generatedAt = (generatedAt != null) ? generatedAt : LocalDateTime.now();
    }

    // ──── Factory methods ─────────────────────────────────────────────────────

    /**
     * Creates a new {@code DiffReport} from the result of a diff analysis.
     * Generates a new UUID and sets {@code generatedAt} to now.
     */
    public static DiffReport create(UUID serviceId, String baseSpecVersion,
                                    String candidateSpecVersion, List<Violation> violations) {
        return new DiffReport(UUID.randomUUID(), serviceId, baseSpecVersion,
                candidateSpecVersion, violations, LocalDateTime.now());
    }

    /**
     * Reconstitutes an existing {@code DiffReport} from persisted state.
     */
    public static DiffReport reconstitute(UUID id, UUID serviceId, String baseSpecVersion,
                                          String candidateSpecVersion, List<Violation> violations,
                                          LocalDateTime generatedAt) {
        return new DiffReport(id, serviceId, baseSpecVersion, candidateSpecVersion,
                violations, generatedAt);
    }

    // ──── Domain behaviour ────────────────────────────────────────────────────

    /**
     * Returns {@code true} if at least one violation with severity {@code BREAKING} exists.
     *
     * <p>This is the primary signal used by GitHub Actions to fail a CI/CD pipeline.
     * A return value of {@code true} means the candidate spec is NOT safe to ship.
     */
    public boolean hasBreakingChanges() {
        return violations.stream().anyMatch(Violation::isBreaking);
    }

    /**
     * Returns {@code true} if this report contains no breaking changes.
     * A compatible report means the candidate spec is safe to ship without breaking consumers.
     */
    public boolean isCompatible() {
        return !hasBreakingChanges();
    }

    /**
     * Returns an immutable list containing only the {@code BREAKING} violations.
     */
    public List<Violation> getBreakingViolations() {
        return violations.stream()
                .filter(Violation::isBreaking)
                .toList();
    }

    /**
     * Returns an immutable list of violations matching the given {@code severity}.
     *
     * @param severity the severity level to filter by; must not be null
     */
    public List<Violation> getViolationsBySeverity(ViolationSeverity severity) {
        return violations.stream()
                .filter(v -> v.severity() == severity)
                .toList();
    }

    /**
     * Returns the total number of violations (across all severity levels).
     */
    public int getViolationCount() {
        return violations.size();
    }
}
