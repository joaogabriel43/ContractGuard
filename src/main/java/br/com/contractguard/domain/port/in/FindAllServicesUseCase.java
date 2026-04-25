package br.com.contractguard.domain.port.in;

import br.com.contractguard.domain.model.catalog.Service;

import java.util.List;

/**
 * Input Port for fetching all services.
 */
public interface FindAllServicesUseCase {
    List<Service> execute();
}
