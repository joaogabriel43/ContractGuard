package br.com.contractguard.domain.port.in;

import br.com.contractguard.domain.model.analyzer.DiffReport;

import java.util.Optional;

/**
 * Input Port for retrieving the latest DiffReport for a service.
 */
public interface GetLatestReportUseCase {
    Optional<DiffReport> execute(String slug);
}
