package br.com.contractguard.presentation.controller;

import br.com.contractguard.domain.exception.DomainException;
import br.com.contractguard.domain.model.analyzer.DiffReport;
import br.com.contractguard.domain.model.analyzer.Violation;
import br.com.contractguard.domain.model.analyzer.ViolationSeverity;
import br.com.contractguard.domain.model.analyzer.ViolationType;
import br.com.contractguard.domain.port.in.AnalyzeContractUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AnalyzerController.class)
@DisplayName("AnalyzerController")
class AnalyzerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalyzeContractUseCase analyzeContractUseCase;

    @Test
    @DisplayName("GIVEN valid request WHEN analyze THEN returns 200 OK with DiffReport")
    void should_analyze_and_return_200() throws Exception {
        var violation = Violation.of("/pets", "GET", ViolationType.ENDPOINT_REMOVED, ViolationSeverity.BREAKING, "Removed");
        DiffReport mockReport = DiffReport.create(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "1.0.0", "2.0.0", List.of(violation));

        when(analyzeContractUseCase.analyze(anyString(), anyString(), anyString())).thenReturn(mockReport);

        String jsonRequest = """
                {
                    "version": "2.0.0",
                    "rawContent": "openapi: 3.0.0"
                }
                """;

        mockMvc.perform(post("/api/v1/services/pet-store/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.hasBreakingChanges").value(true))
                .andExpect(jsonPath("$.violationCount").value(1))
                .andExpect(jsonPath("$.violations[0].ruleType").value("ENDPOINT_REMOVED"));
    }

    @Test
    @DisplayName("GIVEN empty raw content WHEN analyze THEN returns 400 Bad Request")
    void should_return_400_when_validation_fails() throws Exception {
        String jsonRequest = """
                {
                    "version": "2.0.0",
                    "rawContent": ""
                }
                """;

        mockMvc.perform(post("/api/v1/services/pet-store/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid Request Parameters"))
                .andExpect(jsonPath("$.detail").value("Raw content must not be blank"));
    }

    @Test
    @DisplayName("GIVEN service not found WHEN analyze THEN returns 400 Bad Request")
    void should_return_400_when_domain_exception() throws Exception {
        when(analyzeContractUseCase.analyze(anyString(), anyString(), anyString()))
                .thenThrow(new DomainException("Service with slug 'unknown' not found"));

        String jsonRequest = """
                {
                    "version": "2.0.0",
                    "rawContent": "openapi: 3.0.0"
                }
                """;

        mockMvc.perform(post("/api/v1/services/unknown/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Domain Rule Violation"))
                .andExpect(jsonPath("$.detail").value("Service with slug 'unknown' not found"));
    }
}
