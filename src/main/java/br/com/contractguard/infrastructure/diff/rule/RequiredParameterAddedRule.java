package br.com.contractguard.infrastructure.diff.rule;

import br.com.contractguard.domain.model.analyzer.Violation;
import br.com.contractguard.domain.model.analyzer.ViolationType;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Detects if a new required parameter was added to an existing endpoint.
 */
public class RequiredParameterAddedRule implements DiffRule {

    @Override
    public List<Violation> evaluate(OpenAPI baseline, OpenAPI candidate) {
        if (baseline.getPaths() == null || candidate.getPaths() == null) {
            return Collections.emptyList();
        }

        List<Violation> violations = new ArrayList<>();

        candidate.getPaths().forEach((path, candidatePathItem) -> {
            PathItem baselinePathItem = baseline.getPaths().get(path);

            // Only check existing endpoints. New endpoints can have required parameters.
            if (baselinePathItem != null) {
                candidatePathItem.readOperationsMap().forEach((httpMethod, candidateOp) -> {
                    if (baselinePathItem.readOperationsMap().containsKey(httpMethod)) {
                        var baselineOp = baselinePathItem.readOperationsMap().get(httpMethod);
                        var baselineParams = baselineOp.getParameters();
                        var candidateParams = candidateOp.getParameters();

                        if (candidateParams != null) {
                            for (Parameter candidateParam : candidateParams) {
                                boolean isRequired = candidateParam.getRequired() != null && candidateParam.getRequired();
                                if (isRequired) {
                                    boolean existsInBaseline = baselineParams != null && baselineParams.stream()
                                            .anyMatch(p -> p.getName().equals(candidateParam.getName()) && p.getIn().equals(candidateParam.getIn()));

                                    if (!existsInBaseline) {
                                        var type = ViolationType.REQUIRED_PARAM_ADDED;
                                        violations.add(Violation.of(
                                                path,
                                                httpMethod.name(),
                                                type,
                                                type.getDefaultSeverity(),
                                                String.format("Required parameter '%s' (in %s) was added to %s %s",
                                                        candidateParam.getName(), candidateParam.getIn(), httpMethod.name(), path)
                                        ));
                                    }
                                }
                            }
                        }
                    }
                });
            }
        });

        return Collections.unmodifiableList(violations);
    }
}
