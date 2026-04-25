package br.com.contractguard.domain.model.analyzer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ViolationType — Enum with default severity")
class ViolationTypeTest {

    @Test
    void endpoint_removed_should_be_breaking() {
        assertThat(ViolationType.ENDPOINT_REMOVED.getDefaultSeverity()).isEqualTo(ViolationSeverity.BREAKING);
    }

    @Test
    void http_method_changed_should_be_breaking() {
        assertThat(ViolationType.HTTP_METHOD_CHANGED.getDefaultSeverity()).isEqualTo(ViolationSeverity.BREAKING);
    }

    @Test
    void required_param_added_should_be_breaking() {
        assertThat(ViolationType.REQUIRED_PARAM_ADDED.getDefaultSeverity()).isEqualTo(ViolationSeverity.BREAKING);
    }

    @Test
    void param_type_changed_should_be_breaking() {
        assertThat(ViolationType.PARAM_TYPE_CHANGED.getDefaultSeverity()).isEqualTo(ViolationSeverity.BREAKING);
    }

    @Test
    void param_removed_should_be_breaking() {
        assertThat(ViolationType.PARAM_REMOVED.getDefaultSeverity()).isEqualTo(ViolationSeverity.BREAKING);
    }

    @Test
    void type_changed_should_be_breaking() {
        assertThat(ViolationType.TYPE_CHANGED.getDefaultSeverity()).isEqualTo(ViolationSeverity.BREAKING);
    }

    @Test
    void required_field_removed_should_be_breaking() {
        assertThat(ViolationType.REQUIRED_FIELD_REMOVED.getDefaultSeverity()).isEqualTo(ViolationSeverity.BREAKING);
    }

    @Test
    void enum_value_removed_should_be_breaking() {
        assertThat(ViolationType.ENUM_VALUE_REMOVED.getDefaultSeverity()).isEqualTo(ViolationSeverity.BREAKING);
    }

    @Test
    void response_schema_changed_should_be_breaking() {
        assertThat(ViolationType.RESPONSE_SCHEMA_CHANGED.getDefaultSeverity()).isEqualTo(ViolationSeverity.BREAKING);
    }

    @Test
    void security_scheme_changed_should_be_warning() {
        assertThat(ViolationType.SECURITY_SCHEME_CHANGED.getDefaultSeverity()).isEqualTo(ViolationSeverity.WARNING);
    }

    @Test
    void endpoint_added_should_be_info() {
        assertThat(ViolationType.ENDPOINT_ADDED.getDefaultSeverity()).isEqualTo(ViolationSeverity.INFO);
    }

    @Test
    void optional_param_added_should_be_info() {
        assertThat(ViolationType.OPTIONAL_PARAM_ADDED.getDefaultSeverity()).isEqualTo(ViolationSeverity.INFO);
    }

    @Test
    void description_changed_should_be_info() {
        assertThat(ViolationType.DESCRIPTION_CHANGED.getDefaultSeverity()).isEqualTo(ViolationSeverity.INFO);
    }
}
