package br.com.contractguard.domain.model.catalog;

import br.com.contractguard.domain.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Service — Entity")
class ServiceTest {

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("GIVEN valid name and slug WHEN Service.create THEN creates entity")
        void should_create_service_with_valid_name_and_slug() {
            var service = Service.create("Pet Store API", "pet-store-api");
            assertThat(service.getName()).isEqualTo("Pet Store API");
            assertThat(service.getSlug()).isEqualTo("pet-store-api");
        }

        @Test
        @DisplayName("GIVEN Service.create THEN generates a non-null UUID")
        void should_generate_uuid_when_creating_service() {
            var service = Service.create("My API", "my-api");
            assertThat(service.getId()).isNotNull();
        }

        @Test
        @DisplayName("GIVEN Service.create THEN sets createdAt timestamp")
        void should_set_created_at_timestamp() {
            var before = LocalDateTime.now().minusSeconds(1);
            var service = Service.create("My API", "my-api");
            var after = LocalDateTime.now().plusSeconds(1);
            assertThat(service.getCreatedAt()).isBetween(before, after);
        }

        @Test
        @DisplayName("GIVEN uppercase slug WHEN Service.create THEN normalizes to lowercase")
        void should_normalize_slug_to_lowercase() {
            var service = Service.create("My API", "MY-API");
            assertThat(service.getSlug()).isEqualTo("my-api");
        }

        @Test
        @DisplayName("GIVEN existing id WHEN Service.reconstitute THEN preserves the id")
        void should_reconstitute_service_with_existing_id() {
            var id = UUID.randomUUID();
            var createdAt = LocalDateTime.of(2024, 1, 15, 10, 0);
            var service = Service.reconstitute(id, "Pet Store", "pet-store", createdAt);
            assertThat(service.getId()).isEqualTo(id);
            assertThat(service.getCreatedAt()).isEqualTo(createdAt);
        }
    }

    @Nested
    @DisplayName("Validation")
    class Validation {

        @Test
        @DisplayName("GIVEN null name WHEN Service.create THEN throws DomainException")
        void should_throw_when_name_is_null() {
            assertThatThrownBy(() -> Service.create(null, "pet-store"))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("name");
        }

        @Test
        @DisplayName("GIVEN blank name WHEN Service.create THEN throws DomainException")
        void should_throw_when_name_is_blank() {
            assertThatThrownBy(() -> Service.create("   ", "pet-store"))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("name");
        }

        @Test
        @DisplayName("GIVEN null slug WHEN Service.create THEN throws DomainException")
        void should_throw_when_slug_is_null() {
            assertThatThrownBy(() -> Service.create("Pet Store", null))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("slug");
        }

        @Test
        @DisplayName("GIVEN blank slug WHEN Service.create THEN throws DomainException")
        void should_throw_when_slug_is_blank() {
            assertThatThrownBy(() -> Service.create("Pet Store", "   "))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("slug");
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("GIVEN two services with same id WHEN compared THEN they are equal")
        void should_be_equal_when_ids_match() {
            var id = UUID.randomUUID();
            var s1 = Service.reconstitute(id, "Pet Store", "pet-store", LocalDateTime.now());
            var s2 = Service.reconstitute(id, "Different Name", "different-slug", LocalDateTime.now());
            assertThat(s1).isEqualTo(s2);
        }

        @Test
        @DisplayName("GIVEN two services with different ids WHEN compared THEN they are not equal")
        void should_not_be_equal_when_ids_differ() {
            var s1 = Service.create("API A", "api-a");
            var s2 = Service.create("API A", "api-a");
            assertThat(s1).isNotEqualTo(s2);
        }
    }
}
