package br.com.contractguard.domain.port.in;

import br.com.contractguard.domain.model.analyzer.DiffReport;

/**
 * Input Port for analyzing an OpenAPI specification and orchestrating the persistence.
 */
public interface AnalyzeContractUseCase {

    /**
     * Orchestrates the analysis flow:
     * 1. Finds the service by slug.
     * 2. Uploads and saves the new candidate specification.
     * 3. Finds the latest baseline specification (if any).
     * 4. Runs the diff engine to generate a report.
     * 5. Saves the report and violations.
     *
     * @param serviceSlug    the slug of the service to analyze
     * @param newSpecContent the raw content of the new specification
     * @param version        the version of the new specification
     * @return the generated {@link DiffReport}
     * @throws br.com.contractguard.domain.exception.DomainException if service not found, version duplicate, or spec invalid
     */
    DiffReport analyze(String serviceSlug, String newSpecContent, String version);
}
