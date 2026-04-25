package br.com.contractguard.domain.port.in;

import br.com.contractguard.domain.model.catalog.Service;

/**
 * Input Port for registering a new Service.
 */
public interface RegisterServiceUseCase {

    /**
     * Registers a new service in the catalog.
     *
     * @param name the human-readable name of the service
     * @param slug the unique identifier for URLs/references
     * @return the registered Service entity
     * @throws br.com.contractguard.domain.exception.DomainException if the slug already exists or input is invalid
     */
    Service register(String name, String slug);
}
