package pe.edu.vallegrande.ms_distribution.application.services;

import pe.edu.vallegrande.ms_distribution.domain.models.DistributionRoute;
import pe.edu.vallegrande.ms_distribution.infrastructure.dto.request.DistributionRouteCreateRequest;
import pe.edu.vallegrande.ms_distribution.infrastructure.dto.request.DistributionRouteUpdateRequest;
import pe.edu.vallegrande.ms_distribution.infrastructure.dto.response.DistributionRouteResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DistributionRouteService {

    Flux<DistributionRoute> getAll();
    Flux<DistributionRoute> getAllActive();
    Flux<DistributionRoute> getAllInactive();
    Mono<DistributionRoute> getById(String id);
    Mono<DistributionRouteResponse> save(DistributionRouteCreateRequest request);
    Mono<DistributionRoute> update(String id, DistributionRouteUpdateRequest request);
    Mono<Void> delete(String id);
    Mono<DistributionRoute> activate(String id);
    Mono<DistributionRoute> deactivate(String id);
}
