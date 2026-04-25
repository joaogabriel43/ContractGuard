package br.com.contractguard.domain.model.catalog;

import br.com.contractguard.domain.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ApiSpecification — Entity")
class ApiSpecificationTest {

    private static final UUID SERVICE_ID = UUID.randomUUID();
    private static final String RAW_CONTENT = """
            openapi: "3.0.0"
            info:
              title: Pet Store
              version: "1.0.0"
            paths: {}
            """;
    private static final String VERSION = "1.0.0";

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("GIVEN valid data WHEN ApiSpecification.create THEN creates entity")
        void should_create_specification_with_valid_data() {
            var spec = ApiSpecification.create(SERVICE_ID, RAW_CONTENT, VERSION);
            assertThat(spec.getServiceId()).isEqualTo(SERVICE_ID);
            assertThat(spec.getRawContent()).isEqualTo(RAW_CONTENT);
            assertThat(spec.getVersion()).isEqualTo(VERSION);
        }

        @Test
        @DisplayName("GIVEN ApiSpecification.create THEN generates a non-null UUID")
        void should_generate_uuid_on_create() {
            var spec = ApiSpecification.create(SERVICE_ID, RAW_CONTENT, VERSION);
            assertThat(spec.getId()).isNotNull();
        }

        @Test
        @DisplayName("GIVEN ApiSpecification.create THEN sets createdAt timestamp")
        void should_have_creation_timestamp() {
            var before = LocalDateTime.now().minusSeconds(1);
            var spec = ApiSpecification.create(SERVICE_ID, RAW_CONTENT, VERSION);
            var after = LocalDateTime.now().plusSeconds(1);
            assertThat(spec.getCreatedAt()).isBetween(before, after);
        }

        @Test
        @DisplayName("GIVEN existing id WHEN reconstitute THEN preserves all fields")
        void should_reconstitute_with_existing_id() {
            var id = UUID.randomUUID();
            var createdAt = LocalDateTime.of(2024, 3, 10, 12, 0);
            var spec = ApiSpecification.reconstitute(id, SERVICE_ID, RAW_CONTENT, VERSION, createdAt);
            assertThat(spec.getId()).isEqualTo(id);
            assertThat(spec.getCreatedAt()).isEqualTo(createdAt);
        }

        @Test
        @DisplayName("GIVEN version with surrounding whitespace WHEN create THEN trims version")
        void should_trim_version() {
            var spec = ApiSpecification.create(SERVICE_ID, RAW_CONTENT, "  2.0.0  ");
            assertThat(spec.getVersion()).isEqualTo("2.0.0");
        }
    }

    @Nested
    @DisplayName("Validation")
    class Validation {

        @Test
        @DisplayName("GIVEN null serviceId WHEN create THEN throws DomainException")
        void should_throw_when_serviceId_is_null() {
            assertThatThrownBy(() -> ApiSpecification.create(null, RAW_CONTENT, VERSION))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("serviceId");
        }

        @Test
        @DisplayName("GIVEN null rawContent WHEN create THEN throws DomainException")
        void should_throw_when_rawContent_is_null() {
            assertThatThrownBy(() -> ApiSpecification.create(SERVICE_ID, null, VERSION))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("rawContent");
        }

        @Test
        @DisplayName("GIVEN blank rawContent WHEN create THEN throws DomainException")
        void should_throw_when_rawContent_is_blank() {
            assertThatThrownBy(() -> ApiSpecification.create(SERVICE_ID, "   ", VERSION))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("rawContent");
        }

        @Test
        @DisplayName("GIVEN null version WHEN create THEN throws DomainException")
        void should_throw_when_version_is_null() {
            assertThatThrownBy(() -> ApiSpecification.create(SERVICE_ID, RAW_CONTENT, null))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("version");
        }

        @Test
        @DisplayName("GIVEN blank version WHEN create THEN throws DomainException")
        void should_throw_when_version_is_blank() {
            assertThatThrownBy(() -> ApiSpecification.create(SERVICE_ID, RAW_CONTENT, "   "))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("version");
        }
    }
}
