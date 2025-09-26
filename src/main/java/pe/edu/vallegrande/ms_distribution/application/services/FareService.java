package pe.edu.vallegrande.ms_distribution.application.services;

import pe.edu.vallegrande.ms_distribution.domain.models.Fare;
import pe.edu.vallegrande.ms_distribution.infrastructure.dto.request.FareCreateRequest;
import pe.edu.vallegrande.ms_distribution.infrastructure.dto.request.FareUpdateRequest;
import pe.edu.vallegrande.ms_distribution.infrastructure.dto.response.FareResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FareService {

    Flux<Fare> getAllF();
    Flux<Fare> getAllActiveF();
    Flux<Fare> getAllInactiveF();
    Mono<Fare> getByIdFMono(String id);
    Mono<FareResponse> saveF(FareCreateRequest fareRequest);
    Mono<Fare> updateF(String id, FareUpdateRequest request);
    Mono<Void> deleteF(String id);
    Mono<Fare> activateF(String id);
    Mono<Fare> deactivateF(String id);
}
