package br.com.contractguard.domain.port.out;

import br.com.contractguard.domain.model.catalog.ApiSpecification;

import java.util.Optional;
import java.util.UUID;

/**
 * Output Port for the ApiSpecification repository.
 */
public interface ApiSpecificationRepositoryPort {

    /**
     * Saves a new or existing ApiSpecification.
     */
    ApiSpecification save(ApiSpecification specification);

    /**
     * Finds an ApiSpecification by its unique ID.
     */
    Optional<ApiSpecification> findById(UUID id);

    /**
     * Finds the most recent specification for a given service.
     * Often used as the baseline for a new analysis.
     */
    Optional<ApiSpecification> findLatestByServiceId(UUID serviceId);
    
    /**
     * Checks if a specific version of a specification already exists for a service.
     */
    boolean existsByServiceIdAndVersion(UUID serviceId, String version);
}
