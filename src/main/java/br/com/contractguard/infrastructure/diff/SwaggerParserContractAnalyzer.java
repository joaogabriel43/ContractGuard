package br.com.contractguard.infrastructure.diff;

import br.com.contractguard.domain.exception.SpecParsingException;
import br.com.contractguard.domain.model.analyzer.DiffReport;
import br.com.contractguard.domain.model.analyzer.Violation;
import br.com.contractguard.domain.port.out.ContractAnalyzerPort;
import br.com.contractguard.infrastructure.diff.rule.DiffRule;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class SwaggerParserContractAnalyzer implements ContractAnalyzerPort {

    private final List<DiffRule> rules;
    private final OpenAPIV3Parser parser;

    public SwaggerParserContractAnalyzer(List<DiffRule> rules) {
        this.rules = rules;
        this.parser = new OpenAPIV3Parser();
    }

    @Override
    public DiffReport analyze(UUID serviceId, String baselineSpec, String candidateSpec) {
        OpenAPI baselineOpenApi = parseSpec(baselineSpec, "baseline");
        OpenAPI candidateOpenApi = parseSpec(candidateSpec, "candidate");

        List<Violation> allViolations = new ArrayList<>();
        for (DiffRule rule : rules) {
            allViolations.addAll(rule.evaluate(baselineOpenApi, candidateOpenApi));
        }

        String baseVersion = extractVersion(baselineOpenApi);
        String candidateVersion = extractVersion(candidateOpenApi);

        return DiffReport.create(serviceId, baseVersion, candidateVersion, allViolations);
    }

    private OpenAPI parseSpec(String specContent, String specName) {
        SwaggerParseResult result = parser.readContents(specContent, null, null);
        OpenAPI openAPI = result.getOpenAPI();

        if (openAPI == null) {
            throw new SpecParsingException(String.format("Failed to parse %s specification. Errors: %s", specName, result.getMessages()));
        }

        return openAPI;
    }

    private String extractVersion(OpenAPI openAPI) {
        if (openAPI.getInfo() != null && openAPI.getInfo().getVersion() != null) {
            return openAPI.getInfo().getVersion();
        }
        return "unknown";
    }
}
