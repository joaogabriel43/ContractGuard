package br.com.contractguard;

import br.com.contractguard.infrastructure.persistence.repository.ApiSpecificationJpaRepository;
import br.com.contractguard.infrastructure.persistence.repository.DiffReportJpaRepository;
import br.com.contractguard.infrastructure.persistence.repository.ServiceJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("E2E Integration: ContractGuard Core Flow")
class ContractGuardE2ETest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ServiceJpaRepository serviceRepository;

    @Autowired
    private ApiSpecificationJpaRepository apiSpecificationRepository;

    @Autowired
    private DiffReportJpaRepository diffReportRepository;

    private static final String SERVICE_SLUG = "orders-api";

    private static final String BASELINE_SPEC = """
            openapi: 3.0.1
            info:
              title: Orders API
              version: 1.0.0
            paths:
              /orders:
                get:
                  summary: List orders
                  responses:
                    '200':
                      description: OK
            """;

    private static final String BREAKING_SPEC = """
            openapi: 3.0.1
            info:
              title: Orders API
              version: 2.0.0
            paths:
              /orders:
                post:
                  summary: Create order
                  responses:
                    '201':
                      description: Created
            """;

    @Test
    @Order(1)
    @DisplayName("1. Register a new Service")
    void should_register_new_service() throws Exception {
        String jsonPayload = """
                {
                    "name": "Orders System API",
                    "slug": "orders-api"
                }
                """;

        mockMvc.perform(post("/api/v1/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/services/orders-api"));

        assertThat(serviceRepository.findBySlug(SERVICE_SLUG)).isPresent();
    }

    @Test
    @Order(2)
    @DisplayName("2. Upload Baseline Specification (First spec, no breaking changes)")
    void should_upload_baseline_spec_and_return_no_breaking_changes() throws Exception {
        String jsonPayload = String.format("""
                {
                    "version": "1.0.0",
                    "rawContent": %s
                }
                """, escapeJsonString(BASELINE_SPEC));

        mockMvc.perform(post("/api/v1/services/" + SERVICE_SLUG + "/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasBreakingChanges").value(false))
                .andExpect(jsonPath("$.violationCount").value(0))
                .andExpect(jsonPath("$.candidateSpecVersion").value("1.0.0"));

        assertThat(apiSpecificationRepository.findAll()).hasSize(1);
        assertThat(diffReportRepository.findAll()).hasSize(1);
    }

    @Test
    @Order(3)
    @DisplayName("3. Upload Breaking Specification (Removes GET /orders)")
    void should_upload_breaking_spec_and_detect_violations() throws Exception {
        String jsonPayload = String.format("""
                {
                    "version": "2.0.0",
                    "rawContent": %s
                }
                """, escapeJsonString(BREAKING_SPEC));

        mockMvc.perform(post("/api/v1/services/" + SERVICE_SLUG + "/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasBreakingChanges").value(true))
                .andExpect(jsonPath("$.violationCount").value(1))
                .andExpect(jsonPath("$.violations[0].ruleType").value("ENDPOINT_REMOVED"))
                .andExpect(jsonPath("$.violations[0].path").value("/orders"))
                .andExpect(jsonPath("$.violations[0].httpMethod").value("GET"))
                .andExpect(jsonPath("$.candidateSpecVersion").value("2.0.0"));

        assertThat(apiSpecificationRepository.findAll()).hasSize(2);
        assertThat(diffReportRepository.findAll()).hasSize(2);
    }

    private String escapeJsonString(String raw) {
        return "\"" + raw.replace("\n", "\\n").replace("\"", "\\\"") + "\"";
    }
}
