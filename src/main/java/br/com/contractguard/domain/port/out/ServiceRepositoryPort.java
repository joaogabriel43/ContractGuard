package br.com.contractguard.domain.port.out;

import br.com.contractguard.domain.model.catalog.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Output Port for the Service repository.
 */
public interface ServiceRepositoryPort {

    /**
     * Saves a new or existing Service.
     */
    Service save(Service service);

    /**
     * Finds a Service by its unique ID.
     */
    Optional<Service> findById(UUID id);

    /**
     * Finds a Service by its slug.
     */
    Optional<Service> findBySlug(String slug);

    /**
     * Checks if a Service exists with the given slug.
     */
    boolean existsBySlug(String slug);

    /**
     * Retrieves all registered services.
     */
    List<Service> findAll();
}
