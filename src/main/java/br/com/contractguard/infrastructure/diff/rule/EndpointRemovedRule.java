package br.com.contractguard.infrastructure.diff.rule;

import br.com.contractguard.domain.model.analyzer.Violation;
import br.com.contractguard.domain.model.analyzer.ViolationType;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Detects if an endpoint (path + HTTP method) present in the baseline was removed in the candidate.
 */
public class EndpointRemovedRule implements DiffRule {

    @Override
    public List<Violation> evaluate(OpenAPI baseline, OpenAPI candidate) {
        if (baseline.getPaths() == null) {
            return Collections.emptyList();
        }

        List<Violation> violations = new ArrayList<>();

        baseline.getPaths().forEach((path, baselinePathItem) -> {
            PathItem candidatePathItem = (candidate.getPaths() != null) ? candidate.getPaths().get(path) : null;

            if (candidatePathItem == null) {
                // Entire path was removed (all methods under it are considered removed)
                baselinePathItem.readOperationsMap().forEach((httpMethod, operation) -> {
                    violations.add(createViolation(path, httpMethod.name()));
                });
            } else {
                // Path exists, but a specific method might have been removed
                baselinePathItem.readOperationsMap().forEach((httpMethod, operation) -> {
                    if (!candidatePathItem.readOperationsMap().containsKey(httpMethod)) {
                        violations.add(createViolation(path, httpMethod.name()));
                    }
                });
            }
        });

        return Collections.unmodifiableList(violations);
    }

    private Violation createViolation(String path, String httpMethod) {
        var type = ViolationType.ENDPOINT_REMOVED;
        return Violation.of(
                path,
                httpMethod,
                type,
                type.getDefaultSeverity(),
                String.format("Endpoint %s %s was removed", httpMethod, path)
        );
    }
}
