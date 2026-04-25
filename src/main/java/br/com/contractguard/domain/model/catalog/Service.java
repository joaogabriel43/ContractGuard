package br.com.contractguard.domain.model.catalog;

import br.com.contractguard.domain.exception.DomainException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain Entity representing an API producer registered in ContractGuard.
 *
 * <p>Identity is based solely on {@link #id}. Two services with the same id are
 * considered the same entity, regardless of other field values.
 *
 * <p><strong>Domain invariants:</strong>
 * <ul>
 *   <li>{@code name} must not be null or blank.</li>
 *   <li>{@code slug} must not be null or blank; it is always normalized to lowercase.</li>
 * </ul>
 *
 * <p><strong>No Spring, JPA, or Swagger Parser dependencies allowed in this class.</strong>
 */
@Getter
@ToString
@EqualsAndHashCode(of = "id")
public class Service {

    private final UUID id;
    private final String name;
    private final String slug;
    private final LocalDateTime createdAt;

    // ──── Private constructor (forces use of factory methods) ─────────────────

    private Service(UUID id, String name, String slug, LocalDateTime createdAt) {
        if (name == null) {
            throw new DomainException("Service name must not be null");
        }
        if (name.isBlank()) {
            throw new DomainException("Service name must not be blank");
        }
        if (slug == null) {
            throw new DomainException("Service slug must not be null");
        }
        if (slug.isBlank()) {
            throw new DomainException("Service slug must not be blank");
        }

        this.id = (id != null) ? id : UUID.randomUUID();
        this.name = name.trim();
        this.slug = slug.trim().toLowerCase();
        this.createdAt = (createdAt != null) ? createdAt : LocalDateTime.now();
    }

    // ──── Factory methods ─────────────────────────────────────────────────────

    /**
     * Creates a new {@code Service} with a generated UUID and the current timestamp.
     * Use this when registering a new service for the first time.
     */
    public static Service create(String name, String slug) {
        return new Service(UUID.randomUUID(), name, slug, LocalDateTime.now());
    }

    /**
     * Reconstitutes an existing {@code Service} from its persisted state.
     * Use this when loading from the database via an infrastructure adapter.
     */
    public static Service reconstitute(UUID id, String name, String slug, LocalDateTime createdAt) {
        return new Service(id, name, slug, createdAt);
    }
}
