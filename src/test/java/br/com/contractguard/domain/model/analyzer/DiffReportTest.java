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
    private static final String BASE_VERSION = "1.0.0";
    private static final String CANDIDATE_VERSION = "2.0.0";

    // ─── Fixtures ────────────────────────────────────────────────────────────

    private Violation breaking() {
        return Violation.of("/pets", "GET", ViolationType.ENDPOINT_REMOVED,
                ViolationSeverity.BREAKING, "Endpoint GET /pets was removed");
    }

    private Violation warning() {
        return Violation.of("/pets", "POST", ViolationType.SECURITY_SCHEME_CHANGED,
                ViolationSeverity.WARNING, "Security scheme changed");
    }

    private Violation info() {
        return Violation.of("/pets", "GET", ViolationType.DESCRIPTION_CHANGED,
                ViolationSeverity.INFO, "Description changed");
    }

    private DiffReport reportWith(List<Violation> violations) {
        return DiffReport.create(SERVICE_ID, BASE_VERSION, CANDIDATE_VERSION, violations);
    }

    // ─── hasBreakingChanges ───────────────────────────────────────────────────

    @Nested
    @DisplayName("hasBreakingChanges()")
    class HasBreakingChanges {

        @Test
        @DisplayName("GIVEN violations with a BREAKING one WHEN hasBreakingChanges THEN returns true")
        void should_return_true_when_violations_contain_at_least_one_breaking_change() {
            var report = reportWith(List.of(breaking(), warning()));
            assertThat(report.hasBreakingChanges()).isTrue();
        }

        @Test
        @DisplayName("GIVEN only non-breaking violations WHEN hasBreakingChanges THEN returns false")
        void should_return_false_when_all_violations_are_non_breaking() {
            var report = reportWith(List.of(warning(), info()));
            assertThat(report.hasBreakingChanges()).isFalse();
        }

        @Test
        @DisplayName("GIVEN empty violations list WHEN hasBreakingChanges THEN returns false")
        void should_return_false_when_no_violations() {
            var report = reportWith(List.of());
            assertThat(report.hasBreakingChanges()).isFalse();
        }

        @Test
        @DisplayName("GIVEN only one BREAKING violation WHEN hasBreakingChanges THEN returns true")
        void should_return_true_when_single_breaking_violation_exists() {
            var report = reportWith(List.of(breaking()));
            assertThat(report.hasBreakingChanges()).isTrue();
        }
    }

    // ─── isCompatible ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("isCompatible()")
    class IsCompatible {

        @Test
        @DisplayName("GIVEN no breaking changes WHEN isCompatible THEN returns true")
        void should_be_compatible_when_no_breaking_changes() {
            var report = reportWith(List.of(warning()));
            assertThat(report.isCompatible()).isTrue();
        }

        @Test
        @DisplayName("GIVEN breaking changes present WHEN isCompatible THEN returns false")
        void should_be_incompatible_when_has_breaking_changes() {
            var report = reportWith(List.of(breaking()));
            assertThat(report.isCompatible()).isFalse();
        }

        @Test
        @DisplayName("GIVEN empty violations WHEN isCompatible THEN returns true")
        void should_be_compatible_when_no_violations() {
            var report = reportWith(List.of());
            assertThat(report.isCompatible()).isTrue();
        }
    }

    // ─── getBreakingViolations ────────────────────────────────────────────────

    @Nested
    @DisplayName("getBreakingViolations()")
    class GetBreakingViolations {

        @Test
        @DisplayName("GIVEN mixed violations WHEN getBreakingViolations THEN returns only breaking ones")
        void should_filter_only_breaking_violations() {
            var b = breaking();
            var report = reportWith(List.of(b, warning(), info()));
            assertThat(report.getBreakingViolations())
                    .containsExactly(b)
                    .hasSize(1);
        }

        @Test
        @DisplayName("GIVEN no breaking violations WHEN getBreakingViolations THEN returns empty list")
        void should_return_empty_breaking_list_when_no_breaking_violations() {
            var report = reportWith(List.of(warning(), info()));
            assertThat(report.getBreakingViolations()).isEmpty();
        }

        @Test
        @DisplayName("GIVEN multiple breaking violations WHEN getBreakingViolations THEN returns all of them")
        void should_return_all_breaking_violations() {
            var b1 = breaking();
            var b2 = Violation.of("/users", "DELETE", ViolationType.ENDPOINT_REMOVED,
                    ViolationSeverity.BREAKING, "DELETE /users removed");
            var report = reportWith(List.of(b1, warning(), b2));
            assertThat(report.getBreakingViolations()).containsExactlyInAnyOrder(b1, b2);
        }
    }

    // ─── getViolationsBySeverity ──────────────────────────────────────────────

    @Nested
    @DisplayName("getViolationsBySeverity()")
    class GetViolationsBySeverity {

        @Test
        @DisplayName("GIVEN mixed violations WHEN filter by WARNING THEN returns only warnings")
        void should_filter_violations_by_severity_warning() {
            var w = warning();
            var report = reportWith(List.of(breaking(), w, info()));
            assertThat(report.getViolationsBySeverity(ViolationSeverity.WARNING))
                    .containsExactly(w);
        }

        @Test
        @DisplayName("GIVEN mixed violations WHEN filter by INFO THEN returns only info violations")
        void should_filter_violations_by_severity_info() {
            var i = info();
            var report = reportWith(List.of(breaking(), warning(), i));
            assertThat(report.getViolationsBySeverity(ViolationSeverity.INFO))
                    .containsExactly(i);
        }

        @Test
        @DisplayName("GIVEN no violations of given severity WHEN filter THEN returns empty")
        void should_return_empty_when_no_violations_of_severity() {
            var report = reportWith(List.of(warning()));
            assertThat(report.getViolationsBySeverity(ViolationSeverity.BREAKING)).isEmpty();
        }
    }

    // ─── getViolationCount ────────────────────────────────────────────────────

    @Nested
    @DisplayName("getViolationCount()")
    class GetViolationCount {

        @Test
        @DisplayName("GIVEN three violations WHEN getViolationCount THEN returns 3")
        void should_count_total_violations() {
            var report = reportWith(List.of(breaking(), warning(), info()));
            assertThat(report.getViolationCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("GIVEN empty violations WHEN getViolationCount THEN returns 0")
        void should_count_zero_violations_when_empty() {
            var report = reportWith(List.of());
            assertThat(report.getViolationCount()).isEqualTo(0);
        }
    }

    // ─── Immutability ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Immutability")
    class Immutability {

        @Test
        @DisplayName("GIVEN a DiffReport WHEN trying to mutate violations list THEN throws UnsupportedOperationException")
        void should_make_violations_list_unmodifiable() {
            var report = reportWith(List.of(breaking()));
            assertThatThrownBy(() -> report.getViolations().add(warning()))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    // ─── Factory validation ───────────────────────────────────────────────────

    @Nested
    @DisplayName("Factory validation")
    class FactoryValidation {

        @Test
        @DisplayName("GIVEN null serviceId WHEN create THEN throws NullPointerException")
        void should_throw_when_serviceId_is_null() {
            assertThatNullPointerException()
                    .isThrownBy(() -> DiffReport.create(null, BASE_VERSION, CANDIDATE_VERSION, List.of()));
        }

        @Test
        @DisplayName("GIVEN null baseSpecVersion WHEN create THEN throws NullPointerException")
        void should_throw_when_base_version_is_null() {
            assertThatNullPointerException()
                    .isThrownBy(() -> DiffReport.create(SERVICE_ID, null, CANDIDATE_VERSION, List.of()));
        }

        @Test
        @DisplayName("GIVEN null candidateSpecVersion WHEN create THEN throws NullPointerException")
        void should_throw_when_candidate_version_is_null() {
            assertThatNullPointerException()
                    .isThrownBy(() -> DiffReport.create(SERVICE_ID, BASE_VERSION, null, List.of()));
        }

        @Test
        @DisplayName("GIVEN null violations list WHEN create THEN throws NullPointerException")
        void should_throw_when_violations_list_is_null() {
            assertThatNullPointerException()
                    .isThrownBy(() -> DiffReport.create(SERVICE_ID, BASE_VERSION, CANDIDATE_VERSION, null));
        }

        @Test
        @DisplayName("GIVEN valid params WHEN create THEN generates a non-null UUID id")
        void should_generate_uuid_on_create() {
            var report = reportWith(List.of());
            assertThat(report.getId()).isNotNull();
        }

        @Test
        @DisplayName("GIVEN valid params WHEN create THEN sets generatedAt timestamp")
        void should_set_generated_at_on_create() {
            var report = reportWith(List.of());
            assertThat(report.getGeneratedAt()).isNotNull();
        }
    }
}
