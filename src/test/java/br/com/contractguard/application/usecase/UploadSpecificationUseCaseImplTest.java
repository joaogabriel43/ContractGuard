package br.com.contractguard.application.usecase;

import br.com.contractguard.domain.exception.DomainException;
import br.com.contractguard.domain.model.catalog.ApiSpecification;
import br.com.contractguard.domain.model.catalog.Service;
import br.com.contractguard.domain.port.out.ApiSpecificationRepositoryPort;
import br.com.contractguard.domain.port.out.ServiceRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UploadSpecificationUseCaseImpl")
class UploadSpecificationUseCaseImplTest {

    @Mock
    private ServiceRepositoryPort serviceRepositoryPort;

    @Mock
    private ApiSpecificationRepositoryPort apiSpecificationRepositoryPort;

    private UploadSpecificationUseCaseImpl useCase;

    private UUID serviceId;

    @BeforeEach
    void setUp() {
        useCase = new UploadSpecificationUseCaseImpl(serviceRepositoryPort, apiSpecificationRepositoryPort);
        serviceId = UUID.randomUUID();
    }

    @Test
    @DisplayName("GIVEN valid params WHEN upload THEN creates and saves specification")
    void should_upload_specification() {
        var service = Service.reconstitute(serviceId, "Pet Store", "pet-store", LocalDateTime.now());
        when(serviceRepositoryPort.findById(serviceId)).thenReturn(Optional.of(service));
        when(apiSpecificationRepositoryPort.existsByServiceIdAndVersion(serviceId, "1.0.0")).thenReturn(false);
        when(apiSpecificationRepositoryPort.save(any(ApiSpecification.class))).thenAnswer(i -> i.getArgument(0));

        var rawContent = "openapi: 3.0.0";
        var result = useCase.upload(serviceId, rawContent, "1.0.0");

        assertThat(result).isNotNull();
        assertThat(result.getServiceId()).isEqualTo(serviceId);
        assertThat(result.getRawContent()).isEqualTo(rawContent);
        assertThat(result.getVersion()).isEqualTo("1.0.0");

        var captor = ArgumentCaptor.forClass(ApiSpecification.class);
        verify(apiSpecificationRepositoryPort).save(captor.capture());
        var savedSpec = captor.getValue();
        assertThat(savedSpec.getServiceId()).isEqualTo(serviceId);
    }

    @Test
    @DisplayName("GIVEN non-existent serviceId WHEN upload THEN throws DomainException")
    void should_throw_when_service_not_found() {
        when(serviceRepositoryPort.findById(serviceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.upload(serviceId, "openapi: 3.0.0", "1.0.0"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("not found");

        verify(apiSpecificationRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("GIVEN existing version WHEN upload THEN throws DomainException")
    void should_throw_when_version_already_exists() {
        var service = Service.reconstitute(serviceId, "Pet Store", "pet-store", LocalDateTime.now());
        when(serviceRepositoryPort.findById(serviceId)).thenReturn(Optional.of(service));
        when(apiSpecificationRepositoryPort.existsByServiceIdAndVersion(serviceId, "1.0.0")).thenReturn(true);

        assertThatThrownBy(() -> useCase.upload(serviceId, "openapi: 3.0.0", "1.0.0"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("already exists");

        verify(apiSpecificationRepositoryPort, never()).save(any());
    }
}
