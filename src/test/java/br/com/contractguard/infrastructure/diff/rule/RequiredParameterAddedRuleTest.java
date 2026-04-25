package br.com.contractguard.infrastructure.diff.rule;

import br.com.contractguard.domain.model.analyzer.Violation;
import br.com.contractguard.domain.model.analyzer.ViolationSeverity;
import br.com.contractguard.domain.model.analyzer.ViolationType;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.*;

@DisplayName("RequiredParameterAddedRule")
class RequiredParameterAddedRuleTest {

    private RequiredParameterAddedRule rule;
    private OpenAPIV3Parser parser;

    @BeforeEach
    void setUp() {
        rule = new RequiredParameterAddedRule();
        parser = new OpenAPIV3Parser();
    }

    private OpenAPI parse(String filename) throws IOException {
        var stream = Objects.requireNonNull(
                getClass().getResourceAsStream("/specs/" + filename),
                "Fixture not found: " + filename);
        var content = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        return parser.readContents(content, null, null).getOpenAPI();
    }

    @Test
    @DisplayName("GIVEN identical specs WHEN evaluate THEN no violations")
    void should_detect_no_violations_when_specs_are_identical() throws IOException {
        var baseline = parse("baseline-spec.yaml");
        assertThat(rule.evaluate(baseline, baseline)).isEmpty();
    }

    @Test
    @DisplayName("GIVEN required 'status' param added to GET /pets WHEN evaluate THEN one BREAKING violation")
    void should_detect_new_required_query_parameter() throws IOException {
        var baseline = parse("baseline-spec.yaml");
        var candidate = parse("candidate-required-param-added.yaml");

        List<Violation> violations = rule.evaluate(baseline, candidate);

        assertThat(violations).hasSize(1);
        var v = violations.get(0);
        assertThat(v.path()).isEqualTo("/pets");
        assertThat(v.httpMethod()).isEqualTo("GET");
        assertThat(v.type()).isEqualTo(ViolationType.REQUIRED_PARAM_ADDED);
        assertThat(v.severity()).isEqualTo(ViolationSeverity.BREAKING);
        assertThat(v.message()).contains("status");
        assertThat(v.isBreaking()).isTrue();
    }

    @Test
    @DisplayName("GIVEN optional param added WHEN evaluate THEN no violations")
    void should_not_flag_optional_parameter_additions() throws IOException {
        var baseline = parse("baseline-spec.yaml");
        var candidate = parse("candidate-compatible.yaml");
        assertThat(rule.evaluate(baseline, candidate)).isEmpty();
    }

    @Test
    @DisplayName("GIVEN new endpoint in candidate (not in baseline) WHEN evaluate THEN not flagged")
    void should_not_flag_required_params_on_new_endpoints() throws IOException {
        var baseline = parse("baseline-spec.yaml");
        var candidate = parse("candidate-compatible.yaml");
        // /pets/search is new in candidate; its params are not a breaking change
        assertThat(rule.evaluate(baseline, candidate)).isEmpty();
    }

    @Test
    @DisplayName("GIVEN baseline has no paths WHEN evaluate THEN no violations")
    void should_return_empty_when_baseline_has_no_paths() {
        assertThat(rule.evaluate(new OpenAPI(), new OpenAPI())).isEmpty();
    }

    @Test
    @DisplayName("GIVEN violation list WHEN mutated THEN throws UnsupportedOperationException")
    void should_return_unmodifiable_list() throws IOException {
        var baseline = parse("baseline-spec.yaml");
        var candidate = parse("candidate-required-param-added.yaml");
        var violations = rule.evaluate(baseline, candidate);
        assertThatThrownBy(() -> violations.add(null))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
