package br.com.contractguard.domain.port.out;

import br.com.contractguard.domain.model.analyzer.DiffReport;

import java.util.Optional;
import java.util.UUID;

/**
 * Output Port for the DiffReport repository.
 */
public interface DiffReportRepositoryPort {

    /**
     * Saves a new DiffReport along with its violations.
     */
    DiffReport save(DiffReport diffReport);

    /**
     * Finds the latest DiffReport for a specific service ID.
     */
    Optional<DiffReport> findLatestByServiceId(UUID serviceId);
}
