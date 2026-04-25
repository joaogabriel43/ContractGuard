package br.com.contractguard.domain.model.analyzer;

/**
 * Classifies the specific type of change detected between two OpenAPI specifications.
 *
 * <p>Each type carries a {@link #getDefaultSeverity()} that represents the canonical
 * severity for that change. The diff engine may override the severity based on additional
 * context, but the default serves as the baseline for classification.
 *
 * <p><strong>Breaking changes</strong>: removals, type incompatibilities, new required constraints.<br>
 * <strong>Warnings</strong>: security or behavioral changes requiring review.<br>
 * <strong>Info</strong>: backward-compatible additions or documentation changes.
 */
public enum ViolationType {

    // ──── Breaking changes ────────────────────────────────────────────────────

    /** An existing endpoint was removed. Consumers calling it will receive HTTP 404. */
    ENDPOINT_REMOVED(ViolationSeverity.BREAKING),

    /** The HTTP method of an existing endpoint was changed (e.g., GET → POST). */
    HTTP_METHOD_CHANGED(ViolationSeverity.BREAKING),

    /** A new required query/path/header parameter was added. Existing callers are missing it. */
    REQUIRED_PARAM_ADDED(ViolationSeverity.BREAKING),

    /** The data type of an existing parameter was changed (e.g., integer → string). */
    PARAM_TYPE_CHANGED(ViolationSeverity.BREAKING),

    /** An existing parameter was removed. Consumers passing it will have their calls ignored or rejected. */
    PARAM_REMOVED(ViolationSeverity.BREAKING),

    /** The type of a schema field was changed in an incompatible way. */
    TYPE_CHANGED(ViolationSeverity.BREAKING),

    /** A field that was previously optional is now required in the request body. */
    REQUIRED_FIELD_REMOVED(ViolationSeverity.BREAKING),

    /** An existing enum value was removed, breaking consumers that send or expect that value. */
    ENUM_VALUE_REMOVED(ViolationSeverity.BREAKING),

    /** The response schema was changed in an incompatible way (e.g., field removed or renamed). */
    RESPONSE_SCHEMA_CHANGED(ViolationSeverity.BREAKING),

    // ──── Warnings ────────────────────────────────────────────────────────────

    /** Authentication or authorization scheme was altered — requires security review. */
    SECURITY_SCHEME_CHANGED(ViolationSeverity.WARNING),

    // ──── Informational (non-breaking) ────────────────────────────────────────

    /** A new endpoint was added — fully backward-compatible. */
    ENDPOINT_ADDED(ViolationSeverity.INFO),

    /** A new optional parameter was added — existing callers are unaffected. */
    OPTIONAL_PARAM_ADDED(ViolationSeverity.INFO),

    /** A description or title was changed — no behavioral impact. */
    DESCRIPTION_CHANGED(ViolationSeverity.INFO);

    // ──── Metadata ────────────────────────────────────────────────────────────

    private final ViolationSeverity defaultSeverity;

    ViolationType(ViolationSeverity defaultSeverity) {
        this.defaultSeverity = defaultSeverity;
    }

    /**
     * Returns the canonical severity for this type of violation.
     * The diff engine may override this based on context.
     */
    public ViolationSeverity getDefaultSeverity() {
        return defaultSeverity;
    }
}
