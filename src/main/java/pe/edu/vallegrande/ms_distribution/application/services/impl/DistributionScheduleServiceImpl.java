package pe.edu.vallegrande.ms_distribution.application.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pe.edu.vallegrande.ms_distribution.application.services.DistributionScheduleService;
import pe.edu.vallegrande.ms_distribution.domain.enums.Constants;
import pe.edu.vallegrande.ms_distribution.domain.models.DistributionSchedule;
import pe.edu.vallegrande.ms_distribution.infrastructure.dto.request.DistributionScheduleCreateRequest;
import pe.edu.vallegrande.ms_distribution.infrastructure.dto.request.DistributionScheduleUpdateRequest;
import pe.edu.vallegrande.ms_distribution.infrastructure.dto.response.DistributionScheduleResponse;
import pe.edu.vallegrande.ms_distribution.infrastructure.exception.CustomException;
import pe.edu.vallegrande.ms_distribution.infrastructure.repository.DistributionScheduleRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
public class DistributionScheduleServiceImpl implements DistributionScheduleService {

    private final DistributionScheduleRepository repository;

    @Override
    public Flux<DistributionSchedule> getAll() {
        return repository.findAll();
    }

    @Override
    public Flux<DistributionSchedule> getAllActive() {
        return repository.findAllByStatus(Constants.ACTIVE.name());
    }

    @Override
    public Flux<DistributionSchedule> getAllInactive() {
        return repository.findAllByStatus(Constants.INACTIVE.name());
    }

    @Override
    public Mono<DistributionSchedule> getById(String id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        "Schedule not found",
                        "No schedule found with id " + id)));
    }

@Override
public Mono<DistributionScheduleResponse> save(DistributionScheduleCreateRequest request) {
    return generateNextScheduleCode() // ← usamos el generador
        .flatMap(generatedCode ->
            repository.existsByScheduleCode(generatedCode)
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new CustomException(
                                HttpStatus.BAD_REQUEST.value(),
                                "Schedule code already exists",
                                "Schedule code " + generatedCode + " already exists"));
                    }

                    DistributionSchedule schedule = DistributionSchedule.builder()
                            .organizationId(request.getOrganizationId())
                            .scheduleCode(generatedCode) // ← usamos el código generado
                            .zoneId(request.getZoneId())
                            .scheduleName(request.getScheduleName())
                            .daysOfWeek(request.getDaysOfWeek())
                            .startTime(request.getStartTime())
                            .endTime(request.getEndTime())
                            .durationHours(request.getDurationHours())
                            .status(Constants.ACTIVE.name())
                            .createdAt(Instant.now())
                            .build();

                    return repository.save(schedule)
                            .map(saved -> DistributionScheduleResponse.builder()
                                    .id(saved.getId())
                                    .organizationId(saved.getOrganizationId())
                                    .scheduleCode(saved.getScheduleCode())
                                    .scheduleName(saved.getScheduleName())
                                    .zoneId(saved.getZoneId())
                                    .daysOfWeek(saved.getDaysOfWeek())
                                    .startTime(saved.getStartTime())
                                    .endTime(saved.getEndTime())
                                    .durationHours(saved.getDurationHours())
                                    .status(saved.getStatus())
                                    .createdAt(saved.getCreatedAt())
                                    .build());
                })
        );
}


    private static final String SCHEDULE_PREFIX = "HOR";

private Mono<String> generateNextScheduleCode() {
    return repository.findTopByOrderByScheduleCodeDesc()
            .map(last -> {
                String lastCode = last.getScheduleCode(); // ej. HOR007
                int number = 0;
                try {
                    number = Integer.parseInt(lastCode.replace(SCHEDULE_PREFIX, ""));
                } catch (NumberFormatException ignored) {}
                return String.format(SCHEDULE_PREFIX + "%03d", number + 1);
            })
            .defaultIfEmpty(SCHEDULE_PREFIX + "001");
}


    @Override
    public Mono<DistributionSchedule> update(String id, DistributionScheduleUpdateRequest request) {
        return repository.findById(id)
                .flatMap(existing -> {
                    existing.setRouteId(request.getRouteId());
                    existing.setDayOfWeek(request.getDayOfWeek());
                    existing.setStartTime(request.getStartTime());
                    existing.setEndTime(request.getEndTime());
                    existing.setEstimatedDuration(request.getEstimatedDuration());
                    return repository.save(existing);
                });
    }

    @Override
    public Mono<Void> delete(String id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        "Schedule not found",
                        "Cannot delete schedule with id " + id)))
                .flatMap(repository::delete);
    }

    @Override
    public Mono<DistributionSchedule> activate(String id) {
        return changeStatus(id, Constants.ACTIVE.name());
    }

    @Override
    public Mono<DistributionSchedule> deactivate(String id) {
        return changeStatus(id, Constants.INACTIVE.name());
    }

    private Mono<DistributionSchedule> changeStatus(String id, String status) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        "Schedule not found",
                        "Cannot change status of schedule with id " + id)))
                .flatMap(schedule -> {
                    schedule.setStatus(status);
                    return repository.save(schedule);
                });
    }
}
