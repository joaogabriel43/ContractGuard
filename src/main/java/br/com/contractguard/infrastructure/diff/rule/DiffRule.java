package br.com.contractguard.infrastructure.diff.rule;

import br.com.contractguard.domain.model.analyzer.Violation;
import io.swagger.v3.oas.models.OpenAPI;

import java.util.List;

/**
 * Represents a single rule for detecting breaking changes between OpenAPI specifications.
 * <p>
 * Follows the Strategy Pattern. Implementations should focus on a specific type of
 * change (e.g., endpoint removed, required parameter added, schema changed).
 */
public interface DiffRule {

    /**
     * Evaluates the candidate specification against the baseline specification.
     *
     * @param baseline  the existing OpenAPI specification
     * @param candidate the new OpenAPI specification to evaluate
     * @return a list of {@link Violation}s found by this rule, or an empty list if none
     */
    List<Violation> evaluate(OpenAPI baseline, OpenAPI candidate);
}
