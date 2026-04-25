package br.com.contractguard.domain.model.analyzer;

import br.com.contractguard.domain.exception.DomainException;

/**
 * Value Object representing a single detected difference between two OpenAPI specifications.
 *
 * <p>Immutable by design (Java record). Two {@code Violation}s are equal if all their
 * fields are equal — no identity-based equality.
 *
 * <p><strong>Domain invariants enforced in compact constructor:</strong>
 * <ul>
 *   <li>{@code path} must not be null or blank.</li>
 *   <li>{@code type} must not be null.</li>
 *   <li>{@code severity} must not be null.</li>
 *   <li>{@code message} must not be null or blank.</li>
 *   <li>{@code httpMethod} is optional (may be null for non-endpoint-level violations).</li>
 * </ul>
 *
 * @param path       the API path where the violation was detected (e.g., {@code /pets/{id}})
 * @param httpMethod the HTTP method affected, or {@code null} if the violation is schema-level
 * @param type       the classification of the detected change
 * @param severity   the severity of the change (BREAKING, WARNING, INFO)
 * @param message    a human-readable description of the violation
 */
public record Violation(
        String path,
        String httpMethod,
        ViolationType type,
        ViolationSeverity severity,
        String message
) {

    // ──── Compact constructor (validation) ────────────────────────────────────

    public Violation {
        if (path == null) {
            throw new DomainException("Violation path must not be null");
        }
        if (path.isBlank()) {
            throw new DomainException("Violation path must not be blank");
        }
        if (type == null) {
            throw new DomainException("Violation type must not be null");
        }
        if (severity == null) {
            throw new DomainException("Violation severity must not be null");
        }
        if (message == null) {
            throw new DomainException("Violation message must not be null");
        }
        if (message.isBlank()) {
            throw new DomainException("Violation message must not be blank");
        }
        // httpMethod is intentionally allowed to be null (schema-level violations)
    }

    // ──── Domain behaviour ────────────────────────────────────────────────────

    /**
     * Returns {@code true} if this violation represents a breaking change that should
     * cause a CI/CD pipeline to fail.
     */
    public boolean isBreaking() {
        return severity == ViolationSeverity.BREAKING;
    }

    // ──── Factory ─────────────────────────────────────────────────────────────

    /**
     * Convenience factory method — preferred over direct constructor invocation.
     */
    public static Violation of(String path, String httpMethod, ViolationType type,
                               ViolationSeverity severity, String message) {
        return new Violation(path, httpMethod, type, severity, message);
    }
}
