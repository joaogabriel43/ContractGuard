package br.com.contractguard.presentation.controller;

import br.com.contractguard.domain.model.catalog.Service;
import br.com.contractguard.domain.port.in.RegisterServiceUseCase;
import br.com.contractguard.presentation.request.RegisterServiceRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/services")
public class ServiceCatalogController {

    private final RegisterServiceUseCase registerServiceUseCase;

    public ServiceCatalogController(RegisterServiceUseCase registerServiceUseCase) {
        this.registerServiceUseCase = registerServiceUseCase;
    }

    @PostMapping
    public ResponseEntity<Void> registerService(@Valid @RequestBody RegisterServiceRequest request) {
        Service service = registerServiceUseCase.register(request.name(), request.slug());
        return ResponseEntity.created(URI.create("/api/v1/services/" + service.getSlug())).build();
    }
}
