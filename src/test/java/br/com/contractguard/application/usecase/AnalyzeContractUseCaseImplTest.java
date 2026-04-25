package br.com.contractguard.application.usecase;

import br.com.contractguard.domain.exception.DomainException;
import br.com.contractguard.domain.model.analyzer.DiffReport;
import br.com.contractguard.domain.model.analyzer.Violation;
import br.com.contractguard.domain.model.analyzer.ViolationSeverity;
import br.com.contractguard.domain.model.analyzer.ViolationType;
import br.com.contractguard.domain.model.catalog.ApiSpecification;
import br.com.contractguard.domain.model.catalog.Service;
import br.com.contractguard.domain.port.in.UploadSpecificationUseCase;
import br.com.contractguard.domain.port.out.ApiSpecificationRepositoryPort;
import br.com.contractguard.domain.port.out.ContractAnalyzerPort;
import br.com.contractguard.domain.port.out.DiffReportRepositoryPort;
import br.com.contractguard.domain.port.out.ServiceRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnalyzeContractUseCaseImpl")
class AnalyzeContractUseCaseImplTest {

    @Mock
    private ServiceRepositoryPort serviceRepositoryPort;
    @Mock
    private ApiSpecificationRepositoryPort apiSpecificationRepositoryPort;
    @Mock
    private UploadSpecificationUseCase uploadSpecificationUseCase;
    @Mock
    private ContractAnalyzerPort contractAnalyzerPort;
    @Mock
    private DiffReportRepositoryPort diffReportRepositoryPort;

    private AnalyzeContractUseCaseImpl useCase;

    private Service service;
    private ApiSpecification candidateSpec;
    private ApiSpecification baselineSpec;
    
    private final UUID serviceId = UUID.randomUUID();
    private final UUID baselineId = UUID.randomUUID();
    private final UUID candidateId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new AnalyzeContractUseCaseImpl(serviceRepositoryPort, apiSpecificationRepositoryPort,
                uploadSpecificationUseCase, contractAnalyzerPort, diffReportRepositoryPort);

        service = Service.reconstitute(serviceId, "Pet Store", "pet-store", LocalDateTime.now());
        candidateSpec = ApiSpecification.reconstitute(candidateId, serviceId, "openapi: candidate", "2.0.0", LocalDateTime.now());
        baselineSpec = ApiSpecification.reconstitute(baselineId, serviceId, "openapi: baseline", "1.0.0", LocalDateTime.now());
    }

    @Test
    @DisplayName("GIVEN first spec upload (no baseline) WHEN analyze THEN generates empty compatible report")
    void should_orchestrate_first_upload_without_baseline() {
        when(serviceRepositoryPort.findBySlug("pet-store")).thenReturn(Optional.of(service));
        when(apiSpecificationRepositoryPort.findLatestByServiceId(serviceId)).thenReturn(Optional.empty());
        when(uploadSpecificationUseCase.upload(serviceId, "openapi: candidate", "2.0.0")).thenReturn(candidateSpec);
        when(diffReportRepositoryPort.save(any())).thenAnswer(i -> i.getArgument(0));

        var report = useCase.analyze("pet-store", "openapi: candidate", "2.0.0");

        assertThat(report).isNotNull();
        assertThat(report.getBaseApiSpecificationId()).isNull();
        assertThat(report.getBaseSpecVersion()).isNull();
        assertThat(report.getCandidateApiSpecificationId()).isEqualTo(candidateId);
        assertThat(report.getViolationCount()).isZero();
        assertThat(report.isCompatible()).isTrue();

        verify(contractAnalyzerPort, never()).analyze(any(), any(), any(), any(), any());
        verify(diffReportRepositoryPort).save(report);
    }

    @Test
    @DisplayName("GIVEN existing baseline WHEN analyze THEN runs analyzer and saves report")
    void should_orchestrate_upload_with_baseline_and_run_analyzer() {
        when(serviceRepositoryPort.findBySlug("pet-store")).thenReturn(Optional.of(service));
        when(apiSpecificationRepositoryPort.findLatestByServiceId(serviceId)).thenReturn(Optional.of(baselineSpec));
        when(uploadSpecificationUseCase.upload(serviceId, "openapi: candidate", "2.0.0")).thenReturn(candidateSpec);

        var violation = Violation.of("/pets", "GET", ViolationType.ENDPOINT_REMOVED, ViolationSeverity.BREAKING, "Removed");
        var generatedReport = DiffReport.create(serviceId, baselineId, candidateId, "1.0.0", "2.0.0", List.of(violation));
        
        when(contractAnalyzerPort.analyze(serviceId, baselineId, candidateId, "openapi: baseline", "openapi: candidate"))
                .thenReturn(generatedReport);
        when(diffReportRepositoryPort.save(any())).thenAnswer(i -> i.getArgument(0));

        var report = useCase.analyze("pet-store", "openapi: candidate", "2.0.0");

        assertThat(report).isNotNull();
        assertThat(report.getBaseApiSpecificationId()).isEqualTo(baselineId);
        assertThat(report.getCandidateApiSpecificationId()).isEqualTo(candidateId);
        assertThat(report.getViolationCount()).isEqualTo(1);
        assertThat(report.hasBreakingChanges()).isTrue();

        verify(diffReportRepositoryPort).save(report);
    }

    @Test
    @DisplayName("GIVEN invalid slug WHEN analyze THEN throws DomainException")
    void should_throw_when_service_not_found() {
        when(serviceRepositoryPort.findBySlug("unknown-slug")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.analyze("unknown-slug", "spec", "1.0.0"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("not found");

        verify(apiSpecificationRepositoryPort, never()).findLatestByServiceId(any());
        verify(uploadSpecificationUseCase, never()).upload(any(), any(), any());
        verify(diffReportRepositoryPort, never()).save(any());
    }
}
