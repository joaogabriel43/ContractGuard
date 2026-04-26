package br.com.contractguard.domain.port.out;

import br.com.contractguard.domain.model.analyzer.DiffReport;

import java.util.UUID;

/**
 * Output Port for the OpenAPI contract analyzer.
 */
public interface ContractAnalyzerPort {

    /**
     * Analyzes two OpenAPI specifications and returns a detailed report of the differences.
     *
     * @param serviceId                 the ID of the service being analyzed
     * @param baseApiSpecificationId    the ID of the baseline specification
     * @param candidateApiSpecificationId the ID of the new specification
     * @param baselineSpec              the raw content of the currently accepted specification
     * @param candidateSpec             the raw content of the new specification to be evaluated
     * @return a {@link DiffReport} detailing the violations found (if any)
     * @throws br.com.contractguard.domain.exception.SpecParsingException if either spec is invalid
     */
    DiffReport analyze(UUID serviceId, UUID baseApiSpecificationId, UUID candidateApiSpecificationId, String baselineSpec, String candidateSpec);
}
