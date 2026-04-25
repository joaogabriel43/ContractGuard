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

@DisplayName("EndpointRemovedRule")
class EndpointRemovedRuleTest {

    private EndpointRemovedRule rule;
    private OpenAPIV3Parser parser;

    @BeforeEach
    void setUp() {
        rule = new EndpointRemovedRule();
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
    @DisplayName("GIVEN GET /pets removed WHEN evaluate THEN one BREAKING violation")
    void should_detect_removed_get_endpoint() throws IOException {
        var baseline = parse("baseline-spec.yaml");
        var candidate = parse("candidate-endpoint-removed.yaml");

        List<Violation> violations = rule.evaluate(baseline, candidate);

        assertThat(violations).hasSize(1);
        var v = violations.get(0);
        assertThat(v.path()).isEqualTo("/pets");
        assertThat(v.httpMethod()).isEqualTo("GET");
        assertThat(v.type()).isEqualTo(ViolationType.ENDPOINT_REMOVED);
        assertThat(v.severity()).isEqualTo(ViolationSeverity.BREAKING);
        assertThat(v.isBreaking()).isTrue();
    }

    @Test
    @DisplayName("GIVEN compatible candidate (new endpoint added) WHEN evaluate THEN no violations")
    void should_not_flag_added_endpoints_as_violations() throws IOException {
        var baseline = parse("baseline-spec.yaml");
        var candidate = parse("candidate-compatible.yaml");
        assertThat(rule.evaluate(baseline, candidate)).isEmpty();
    }

    @Test
    @DisplayName("GIVEN baseline has no paths WHEN evaluate THEN no violations")
    void should_return_empty_when_baseline_has_no_paths() {
        assertThat(rule.evaluate(new OpenAPI(), new OpenAPI())).isEmpty();
    }

    @Test
    @DisplayName("GIVEN candidate has null paths WHEN evaluate THEN all baseline endpoints flagged")
    void should_flag_all_endpoints_when_candidate_has_no_paths() throws IOException {
        var baseline = parse("baseline-spec.yaml");
        var candidate = new OpenAPI();

        List<Violation> violations = rule.evaluate(baseline, candidate);

        // baseline has: GET /pets, POST /pets, GET /pets/{petId} → 3 violations
        assertThat(violations).hasSize(3);
        assertThat(violations).allMatch(v -> v.severity() == ViolationSeverity.BREAKING);
        assertThat(violations).allMatch(v -> v.type() == ViolationType.ENDPOINT_REMOVED);
    }

    @Test
    @DisplayName("GIVEN violation list WHEN mutated THEN throws UnsupportedOperationException")
    void should_return_unmodifiable_list() throws IOException {
        var baseline = parse("baseline-spec.yaml");
        var candidate = parse("candidate-endpoint-removed.yaml");
        var violations = rule.evaluate(baseline, candidate);
        assertThatThrownBy(() -> violations.add(null))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
