package br.com.contractguard.domain.model.analyzer;

import br.com.contractguard.domain.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Violation — Value Object")
class ViolationTest {

    // ─── Happy path ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("GIVEN all valid fields WHEN Violation.of THEN creates violation correctly")
        void should_create_violation_with_all_required_fields() {
            var v = Violation.of("/pets", "GET", ViolationType.ENDPOINT_REMOVED,
                    ViolationSeverity.BREAKING, "GET /pets was removed");

            assertThat(v.path()).isEqualTo("/pets");
            assertThat(v.httpMethod()).isEqualTo("GET");
            assertThat(v.type()).isEqualTo(ViolationType.ENDPOINT_REMOVED);
            assertThat(v.severity()).isEqualTo(ViolationSeverity.BREAKING);
            assertThat(v.message()).isEqualTo("GET /pets was removed");
        }

        @Test
        @DisplayName("GIVEN null httpMethod WHEN Violation.of THEN creates violation (httpMethod is optional)")
        void should_accept_null_http_method() {
            var v = Violation.of("/openapi.json", null, ViolationType.TYPE_CHANGED,
                    ViolationSeverity.BREAKING, "Global schema type changed");

            assertThat(v.httpMethod()).isNull();
        }
    }

    // ─── isBreaking ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("isBreaking()")
    class IsBreaking {

        @Test
        @DisplayName("GIVEN severity BREAKING WHEN isBreaking THEN returns true")
        void should_be_breaking_when_severity_is_breaking() {
            var v = Violation.of("/users", "DELETE", ViolationType.ENDPOINT_REMOVED,
                    ViolationSeverity.BREAKING, "removed");
            assertThat(v.isBreaking()).isTrue();
        }

        @Test
        @DisplayName("GIVEN severity WARNING WHEN isBreaking THEN returns false")
        void should_not_be_breaking_when_severity_is_warning() {
            var v = Violation.of("/users", "GET", ViolationType.SECURITY_SCHEME_CHANGED,
                    ViolationSeverity.WARNING, "security changed");
            assertThat(v.isBreaking()).isFalse();
        }

        @Test
        @DisplayName("GIVEN severity INFO WHEN isBreaking THEN returns false")
        void should_not_be_breaking_when_severity_is_info() {
            var v = Violation.of("/users", "GET", ViolationType.DESCRIPTION_CHANGED,
                    ViolationSeverity.INFO, "description changed");
            assertThat(v.isBreaking()).isFalse();
        }
    }

    // ─── Validation ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Validation")
    class Validation {

        @Test
        @DisplayName("GIVEN null path WHEN Violation.of THEN throws DomainException")
        void should_throw_when_path_is_null() {
            assertThatThrownBy(() ->
                    Violation.of(null, "GET", ViolationType.ENDPOINT_REMOVED,
                            ViolationSeverity.BREAKING, "msg"))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("path");
        }

        @Test
        @DisplayName("GIVEN blank path WHEN Violation.of THEN throws DomainException")
        void should_throw_when_path_is_blank() {
            assertThatThrownBy(() ->
                    Violation.of("   ", "GET", ViolationType.ENDPOINT_REMOVED,
                            ViolationSeverity.BREAKING, "msg"))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("path");
        }

        @Test
        @DisplayName("GIVEN null type WHEN Violation.of THEN throws DomainException")
        void should_throw_when_type_is_null() {
            assertThatThrownBy(() ->
                    Violation.of("/pets", "GET", null,
                            ViolationSeverity.BREAKING, "msg"))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("type");
        }

        @Test
        @DisplayName("GIVEN null severity WHEN Violation.of THEN throws DomainException")
        void should_throw_when_severity_is_null() {
            assertThatThrownBy(() ->
                    Violation.of("/pets", "GET", ViolationType.ENDPOINT_REMOVED,
                            null, "msg"))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("severity");
        }

        @Test
        @DisplayName("GIVEN null message WHEN Violation.of THEN throws DomainException")
        void should_throw_when_message_is_null() {
            assertThatThrownBy(() ->
                    Violation.of("/pets", "GET", ViolationType.ENDPOINT_REMOVED,
                            ViolationSeverity.BREAKING, null))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("message");
        }

        @Test
        @DisplayName("GIVEN blank message WHEN Violation.of THEN throws DomainException")
        void should_throw_when_message_is_blank() {
            assertThatThrownBy(() ->
                    Violation.of("/pets", "GET", ViolationType.ENDPOINT_REMOVED,
                            ViolationSeverity.BREAKING, "   "))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("message");
        }
    }

    // ─── Value Object equality ────────────────────────────────────────────────

    @Nested
    @DisplayName("Value Object equality (record)")
    class Equality {

        @Test
        @DisplayName("GIVEN two violations with same data WHEN compared THEN they are equal")
        void should_be_equal_when_all_fields_match() {
            var v1 = Violation.of("/pets", "GET", ViolationType.ENDPOINT_REMOVED,
                    ViolationSeverity.BREAKING, "removed");
            var v2 = Violation.of("/pets", "GET", ViolationType.ENDPOINT_REMOVED,
                    ViolationSeverity.BREAKING, "removed");
            assertThat(v1).isEqualTo(v2);
        }

        @Test
        @DisplayName("GIVEN two violations with different paths WHEN compared THEN they are not equal")
        void should_not_be_equal_when_paths_differ() {
            var v1 = Violation.of("/pets", "GET", ViolationType.ENDPOINT_REMOVED,
                    ViolationSeverity.BREAKING, "removed");
            var v2 = Violation.of("/users", "GET", ViolationType.ENDPOINT_REMOVED,
                    ViolationSeverity.BREAKING, "removed");
            assertThat(v1).isNotEqualTo(v2);
        }
    }
}
