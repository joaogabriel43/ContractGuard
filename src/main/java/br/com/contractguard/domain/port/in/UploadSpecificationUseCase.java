package br.com.contractguard.domain.port.in;

import br.com.contractguard.domain.model.catalog.ApiSpecification;

import java.util.UUID;

/**
 * Input Port for uploading a new OpenAPI specification.
 */
public interface UploadSpecificationUseCase {

    /**
     * Uploads a new API specification for an existing service.
     *
     * @param serviceId  the ID of the service
     * @param rawContent the raw OpenAPI specification content (YAML/JSON)
     * @param version    the version of the specification
     * @return the created ApiSpecification entity
     * @throws br.com.contractguard.domain.exception.DomainException if the service does not exist or version already exists
     */
    ApiSpecification upload(UUID serviceId, String rawContent, String version);
}
