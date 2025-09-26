package pe.edu.vallegrande.ms_distribution.application.services;

import pe.edu.vallegrande.ms_distribution.domain.models.DistributionSchedule;
import pe.edu.vallegrande.ms_distribution.infrastructure.dto.request.DistributionScheduleCreateRequest;
import pe.edu.vallegrande.ms_distribution.infrastructure.dto.request.DistributionScheduleUpdateRequest;
import pe.edu.vallegrande.ms_distribution.infrastructure.dto.response.DistributionScheduleResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DistributionScheduleService {

    Flux<DistributionSchedule> getAll();
    Flux<DistributionSchedule> getAllActive();
    Flux<DistributionSchedule> getAllInactive();
    Mono<DistributionSchedule> getById(String id);
    Mono<DistributionScheduleResponse> save(DistributionScheduleCreateRequest request);
    Mono<DistributionSchedule> update(String id, DistributionScheduleUpdateRequest request);
    Mono<Void> delete(String id);
    Mono<DistributionSchedule> activate(String id);
    Mono<DistributionSchedule> deactivate(String id);
}
