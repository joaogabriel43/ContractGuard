package br.com.contractguard.application.usecase;

import br.com.contractguard.domain.model.catalog.Service;
import br.com.contractguard.domain.port.in.FindAllServicesUseCase;
import br.com.contractguard.domain.port.out.ServiceRepositoryPort;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class FindAllServicesUseCaseImpl implements FindAllServicesUseCase {

  private final ServiceRepositoryPort repositoryPort;

  public FindAllServicesUseCaseImpl(ServiceRepositoryPort repositoryPort) {
    this.repositoryPort = repositoryPort;
  }

  @Override
  public List<Service> execute() {
    return repositoryPort.findAll();
  }
}
