package br.com.contractguard.domain.model.catalog;

import br.com.contractguard.domain.exception.DomainException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain Entity representing a versioned snapshot of an OpenAPI specification
 * belonging to a {@link Service}.
 *
 * <p>The {@code rawContent} field stores the full YAML or JSON content of the spec.
 * Parsing and structural analysis are delegated to the {@code infrastructure.diff} adapter,
 * keeping this entity free of any external library dependencies.
 *
 * <p><strong>Domain invariants:</strong>
 * <ul>
 *   <li>{@code serviceId} must not be null.</li>
 *   <li>{@code rawContent} must not be null or blank.</li>
 *   <li>{@code version} must not be null or blank; surrounding whitespace is trimmed.</li>
 * </ul>
 *
 * <p><strong>No Spring, JPA, or Swagger Parser dependencies allowed in this class.</strong>
 */
@Getter
@ToString
@EqualsAndHashCode(of = "id")
public class ApiSpecification {

    private final UUID id;
    private final UUID serviceId;
    private final String rawContent;
    private final String version;
    private final LocalDateTime createdAt;

    // ──── Private constructor (forces use of factory methods) ─────────────────

    private ApiSpecification(UUID id, UUID serviceId, String rawContent,
                             String version, LocalDateTime createdAt) {
        if (serviceId == null) {
            throw new DomainException("ApiSpecification serviceId must not be null");
        }
        if (rawContent == null) {
            throw new DomainException("ApiSpecification rawContent must not be null");
        }
        if (rawContent.isBlank()) {
            throw new DomainException("ApiSpecification rawContent must not be blank");
        }
        if (version == null) {
            throw new DomainException("ApiSpecification version must not be null");
        }
        if (version.isBlank()) {
            throw new DomainException("ApiSpecification version must not be blank");
        }

        this.id = (id != null) ? id : UUID.randomUUID();
        this.serviceId = serviceId;
        this.rawContent = rawContent;
        this.version = version.trim();
        this.createdAt = (createdAt != null) ? createdAt : LocalDateTime.now();
    }

    // ──── Factory methods ─────────────────────────────────────────────────────

    /**
     * Creates a new {@code ApiSpecification} with a generated UUID and the current timestamp.
     * Use this when uploading a spec for the first time.
     */
    public static ApiSpecification create(UUID serviceId, String rawContent, String version) {
        return new ApiSpecification(UUID.randomUUID(), serviceId, rawContent, version, LocalDateTime.now());
    }

    /**
     * Reconstitutes an existing {@code ApiSpecification} from its persisted state.
     * Use this when loading from the database via an infrastructure adapter.
     */
    public static ApiSpecification reconstitute(UUID id, UUID serviceId, String rawContent,
                                                String version, LocalDateTime createdAt) {
        return new ApiSpecification(id, serviceId, rawContent, version, createdAt);
    }
}
