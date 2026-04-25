package br.com.contractguard.application.usecase;

import br.com.contractguard.domain.exception.DomainException;
import br.com.contractguard.domain.model.catalog.Service;
import br.com.contractguard.domain.port.out.ServiceRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterServiceUseCaseImpl")
class RegisterServiceUseCaseImplTest {

    @Mock
    private ServiceRepositoryPort serviceRepositoryPort;

    private RegisterServiceUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new RegisterServiceUseCaseImpl(serviceRepositoryPort);
    }

    @Test
    @DisplayName("GIVEN valid name and slug WHEN register THEN creates and saves service")
    void should_register_service() {
        when(serviceRepositoryPort.existsBySlug("pet-store")).thenReturn(false);
        when(serviceRepositoryPort.save(any(Service.class))).thenAnswer(i -> i.getArgument(0));

        var result = useCase.register("Pet Store", "pet-store");

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Pet Store");
        assertThat(result.getSlug()).isEqualTo("pet-store");

        var captor = ArgumentCaptor.forClass(Service.class);
        verify(serviceRepositoryPort).save(captor.capture());
        
        var savedService = captor.getValue();
        assertThat(savedService.getName()).isEqualTo("Pet Store");
        assertThat(savedService.getSlug()).isEqualTo("pet-store");
    }

    @Test
    @DisplayName("GIVEN existing slug WHEN register THEN throws DomainException")
    void should_throw_when_slug_already_exists() {
        when(serviceRepositoryPort.existsBySlug("pet-store")).thenReturn(true);

        assertThatThrownBy(() -> useCase.register("Pet Store", "pet-store"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("already exists");

        verify(serviceRepositoryPort, never()).save(any());
    }
}
