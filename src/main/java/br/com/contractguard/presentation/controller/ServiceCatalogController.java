package br.com.contractguard.presentation.controller;

import br.com.contractguard.domain.model.catalog.Service;
import br.com.contractguard.domain.port.in.FindAllServicesUseCase;
import br.com.contractguard.domain.port.in.RegisterServiceUseCase;
import br.com.contractguard.presentation.request.RegisterServiceRequest;
import br.com.contractguard.presentation.response.ServiceResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/services")
public class ServiceCatalogController {

    private final RegisterServiceUseCase registerServiceUseCase;
    private final FindAllServicesUseCase findAllServicesUseCase;

    public ServiceCatalogController(RegisterServiceUseCase registerServiceUseCase,
                                    FindAllServicesUseCase findAllServicesUseCase) {
        this.registerServiceUseCase = registerServiceUseCase;
        this.findAllServicesUseCase = findAllServicesUseCase;
    }

    @PostMapping
    public ResponseEntity<Void> registerService(@Valid @RequestBody RegisterServiceRequest request) {
        Service service = registerServiceUseCase.register(request.name(), request.slug());
        return ResponseEntity.created(URI.create("/api/v1/services/" + service.getSlug())).build();
    }

    @GetMapping
    public ResponseEntity<List<ServiceResponse>> findAll() {
        List<ServiceResponse> responses = findAllServicesUseCase.execute().stream()
                .map(s -> new ServiceResponse(s.getId(), s.getName(), s.getSlug(), s.getCreatedAt()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
}
