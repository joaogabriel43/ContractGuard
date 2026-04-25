package br.com.contractguard.presentation.controller;

import br.com.contractguard.domain.exception.DomainException;
import br.com.contractguard.domain.model.catalog.Service;
import br.com.contractguard.domain.port.in.RegisterServiceUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ServiceCatalogController.class)
@DisplayName("ServiceCatalogController")
class ServiceCatalogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RegisterServiceUseCase registerServiceUseCase;

    @Test
    @DisplayName("GIVEN valid request WHEN register service THEN returns 201 Created")
    void should_register_service_and_return_201() throws Exception {
        Service mockService = Service.create("Pet Store", "pet-store");
        when(registerServiceUseCase.register(anyString(), anyString())).thenReturn(mockService);

        String jsonRequest = """
                {
                    "name": "Pet Store",
                    "slug": "pet-store"
                }
                """;

        mockMvc.perform(post("/api/v1/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location", "/api/v1/services/pet-store"));
    }

    @Test
    @DisplayName("GIVEN invalid slug format WHEN register service THEN returns 400 Bad Request")
    void should_return_400_when_slug_is_invalid() throws Exception {
        String jsonRequest = """
                {
                    "name": "Pet Store",
                    "slug": "INVALID SLUG!"
                }
                """;

        mockMvc.perform(post("/api/v1/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid Request Parameters"))
                .andExpect(jsonPath("$.detail").value("Slug must contain only lowercase letters, numbers, and hyphens"));
    }

    @Test
    @DisplayName("GIVEN domain exception WHEN register service THEN returns 400 Bad Request")
    void should_return_400_when_domain_exception_thrown() throws Exception {
        when(registerServiceUseCase.register(anyString(), anyString()))
                .thenThrow(new DomainException("Service with slug 'pet-store' already exists"));

        String jsonRequest = """
                {
                    "name": "Pet Store",
                    "slug": "pet-store"
                }
                """;

        mockMvc.perform(post("/api/v1/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Domain Rule Violation"))
                .andExpect(jsonPath("$.detail").value("Service with slug 'pet-store' already exists"));
    }
}
