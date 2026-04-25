package br.com.contractguard.domain.model.analyzer;

/**
 * Classifies the severity of a detected API contract violation.
 *
 * <ul>
 *   <li>{@link #BREAKING} — the change is incompatible and will break existing consumers.
 *       CI/CD pipelines must fail when this is detected.</li>
 *   <li>{@link #WARNING} — the change is potentially problematic but not necessarily fatal.
 *       Requires human review before merging.</li>
 *   <li>{@link #INFO} — the change is backward-compatible and purely informational
 *       (e.g., a new endpoint or an optional parameter was added).</li>
 * </ul>
 */
public enum ViolationSeverity {

    /** Incompatible change — breaks existing API consumers. */
    BREAKING,

    /** Potentially problematic change — requires review. */
    WARNING,

    /** Backward-compatible change — informational only. */
    INFO
}
