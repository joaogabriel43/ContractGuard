package br.com.contractguard.infrastructure.diff;

import br.com.contractguard.domain.exception.SpecParsingException;
import br.com.contractguard.domain.model.analyzer.Violation;
import br.com.contractguard.domain.model.analyzer.ViolationSeverity;
import br.com.contractguard.domain.model.analyzer.ViolationType;
import br.com.contractguard.infrastructure.diff.rule.DiffRule;
import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SwaggerParserContractAnalyzer")
class SwaggerParserContractAnalyzerTest {

    private static final UUID SERVICE_ID = UUID.randomUUID();

    // Minimal valid OpenAPI 3 spec strings for testing the orchestration layer
    private static final String SPEC_V1 = """
            openapi: "3.0.3"
            info:
              title: "Test API"
              version: "1.0.0"
            paths: {}
            """;

    private static final String SPEC_V2 = """
            openapi: "3.0.3"
            info:
              title: "Test API"
              version: "2.0.0"
            paths: {}
            """;

    private static final String INVALID_SPEC = "this is: not: valid: openapi: {{{";

    @Mock
    private DiffRule rule1;

    @Mock
    private DiffRule rule2;

    private SwaggerParserContractAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new SwaggerParserContractAnalyzer(List.of(rule1, rule2));
    }

    @Test
    @DisplayName("GIVEN no violations from rules WHEN analyze THEN returns compatible empty report")
    void should_return_empty_compatible_report_when_no_violations() {
        when(rule1.evaluate(any(), any())).thenReturn(List.of());
        when(rule2.evaluate(any(), any())).thenReturn(List.of());

        var report = analyzer.analyze(SERVICE_ID, UUID.randomUUID(), UUID.randomUUID(), SPEC_V1, SPEC_V2);

        assertThat(report.getViolationCount()).isZero();
        assertThat(report.isCompatible()).isTrue();
        assertThat(report.hasBreakingChanges()).isFalse();
    }

    @Test
    @DisplayName("GIVEN violations from multiple rules WHEN analyze THEN aggregates all violations")
    void should_aggregate_violations_from_all_rules() {
        var v1 = Violation.of("/pets", "GET", ViolationType.ENDPOINT_REMOVED,
                ViolationSeverity.BREAKING, "removed");
        var v2 = Violation.of("/users", "POST", ViolationType.REQUIRED_PARAM_ADDED,
                ViolationSeverity.BREAKING, "required param added");

        when(rule1.evaluate(any(), any())).thenReturn(List.of(v1));
        when(rule2.evaluate(any(), any())).thenReturn(List.of(v2));

        var report = analyzer.analyze(SERVICE_ID, UUID.randomUUID(), UUID.randomUUID(), SPEC_V1, SPEC_V2);

        assertThat(report.getViolationCount()).isEqualTo(2);
        assertThat(report.hasBreakingChanges()).isTrue();
        assertThat(report.getViolations()).containsExactlyInAnyOrder(v1, v2);
    }

    @Test
    @DisplayName("GIVEN valid specs WHEN analyze THEN all rules are invoked exactly once")
    void should_call_all_rules_exactly_once() {
        when(rule1.evaluate(any(), any())).thenReturn(List.of());
        when(rule2.evaluate(any(), any())).thenReturn(List.of());

        analyzer.analyze(SERVICE_ID, UUID.randomUUID(), UUID.randomUUID(), SPEC_V1, SPEC_V2);

        verify(rule1, times(1)).evaluate(any(OpenAPI.class), any(OpenAPI.class));
        verify(rule2, times(1)).evaluate(any(OpenAPI.class), any(OpenAPI.class));
    }

    @Test
    @DisplayName("GIVEN valid specs WHEN analyze THEN report contains correct serviceId")
    void should_set_serviceId_on_report() {
        when(rule1.evaluate(any(), any())).thenReturn(List.of());
        when(rule2.evaluate(any(), any())).thenReturn(List.of());

        var report = analyzer.analyze(SERVICE_ID, UUID.randomUUID(), UUID.randomUUID(), SPEC_V1, SPEC_V2);

        assertThat(report.getServiceId()).isEqualTo(SERVICE_ID);
    }

    @Test
    @DisplayName("GIVEN specs with version info WHEN analyze THEN report has correct versions")
    void should_extract_versions_from_spec_info() {
        when(rule1.evaluate(any(), any())).thenReturn(List.of());
        when(rule2.evaluate(any(), any())).thenReturn(List.of());

        var report = analyzer.analyze(SERVICE_ID, UUID.randomUUID(), UUID.randomUUID(), SPEC_V1, SPEC_V2);

        assertThat(report.getBaseSpecVersion()).isEqualTo("1.0.0");
        assertThat(report.getCandidateSpecVersion()).isEqualTo("2.0.0");
    }

    @Test
    @DisplayName("GIVEN invalid baseline spec WHEN analyze THEN throws SpecParsingException")
    void should_throw_spec_parsing_exception_when_baseline_is_invalid() {
        assertThatThrownBy(() -> analyzer.analyze(SERVICE_ID, UUID.randomUUID(), UUID.randomUUID(), INVALID_SPEC, SPEC_V2))
                .isInstanceOf(SpecParsingException.class)
                .hasMessageContaining("baseline");
    }

    @Test
    @DisplayName("GIVEN invalid candidate spec WHEN analyze THEN throws SpecParsingException")
    void should_throw_spec_parsing_exception_when_candidate_is_invalid() {
        assertThatThrownBy(() -> analyzer.analyze(SERVICE_ID, UUID.randomUUID(), UUID.randomUUID(), SPEC_V1, INVALID_SPEC))
                .isInstanceOf(SpecParsingException.class)
                .hasMessageContaining("candidate");
    }

    @Test
    @DisplayName("GIVEN report WHEN violations mutated THEN throws UnsupportedOperationException")
    void should_return_report_with_unmodifiable_violations() {
        when(rule1.evaluate(any(), any())).thenReturn(List.of());
        when(rule2.evaluate(any(), any())).thenReturn(List.of());

        var report = analyzer.analyze(SERVICE_ID, UUID.randomUUID(), UUID.randomUUID(), SPEC_V1, SPEC_V2);

        assertThatThrownBy(() -> report.getViolations().add(null))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
