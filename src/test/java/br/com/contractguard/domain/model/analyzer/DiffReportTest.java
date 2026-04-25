package br.com.contractguard.domain.model.analyzer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("DiffReport — Aggregate")
class DiffReportTest {

    private static final UUID SERVICE_ID = UUID.randomUUID();
    private static final UUID BASE_SPEC_ID = UUID.randomUUID();
    private static final UUID CANDIDATE_SPEC_ID = UUID.randomUUID();
    private static final String BASE_VERSION = "1.0.0";
    private static final String CANDIDATE_VERSION = "2.0.0";

    private Violation breaking() {
        return Violation.of("/pets", "GET", ViolationType.ENDPOINT_REMOVED,
                ViolationSeverity.BREAKING, "Endpoint GET /pets was removed");
    }

    private DiffReport reportWith(List<Violation> violations) {
        return DiffReport.create(SERVICE_ID, BASE_SPEC_ID, CANDIDATE_SPEC_ID, BASE_VERSION, CANDIDATE_VERSION, violations);
    }

    @Nested
    @DisplayName("Factory validation")
    class FactoryValidation {
        @Test
        @DisplayName("GIVEN no baseline WHEN createWithoutBaseline THEN creates empty successful report")
        void should_create_without_baseline() {
            var report = DiffReport.createWithoutBaseline(SERVICE_ID, CANDIDATE_SPEC_ID, CANDIDATE_VERSION);
            assertThat(report.getBaseApiSpecificationId()).isNull();
            assertThat(report.getBaseSpecVersion()).isNull();
            assertThat(report.getCandidateApiSpecificationId()).isEqualTo(CANDIDATE_SPEC_ID);
            assertThat(report.getViolationCount()).isZero();
            assertThat(report.isCompatible()).isTrue();
        }
        
        @Test
        @DisplayName("GIVEN null serviceId WHEN create THEN throws NullPointerException")
        void should_throw_when_serviceId_is_null() {
            assertThatNullPointerException()
                    .isThrownBy(() -> DiffReport.create(null, BASE_SPEC_ID, CANDIDATE_SPEC_ID, BASE_VERSION, CANDIDATE_VERSION, List.of()));
        }

        @Test
        @DisplayName("GIVEN null candidateSpecId WHEN create THEN throws NullPointerException")
        void should_throw_when_candidate_id_is_null() {
            assertThatNullPointerException()
                    .isThrownBy(() -> DiffReport.create(SERVICE_ID, BASE_SPEC_ID, null, BASE_VERSION, CANDIDATE_VERSION, List.of()));
        }
    }
    
    @Nested
    @DisplayName("hasBreakingChanges()")
    class HasBreakingChanges {

        @Test
        @DisplayName("GIVEN violations with a BREAKING one WHEN hasBreakingChanges THEN returns true")
        void should_return_true_when_violations_contain_at_least_one_breaking_change() {
            var report = reportWith(List.of(breaking()));
            assertThat(report.hasBreakingChanges()).isTrue();
        }
    }
}
