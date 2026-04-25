package br.com.contractguard.application.usecase;

import br.com.contractguard.domain.exception.DomainException;
import br.com.contractguard.domain.model.catalog.Service;
import br.com.contractguard.domain.port.in.RegisterServiceUseCase;
import br.com.contractguard.domain.port.out.ServiceRepositoryPort;
import org.springframework.stereotype.Component;

@Component
public class RegisterServiceUseCaseImpl implements RegisterServiceUseCase {

    private final ServiceRepositoryPort serviceRepositoryPort;

    public RegisterServiceUseCaseImpl(ServiceRepositoryPort serviceRepositoryPort) {
        this.serviceRepositoryPort = serviceRepositoryPort;
    }

    @Override
    public Service register(String name, String slug) {
        if (serviceRepositoryPort.existsBySlug(slug)) {
            throw new DomainException(String.format("Service with slug '%s' already exists", slug));
        }

        Service service = Service.create(name, slug);
        return serviceRepositoryPort.save(service);
    }
}
