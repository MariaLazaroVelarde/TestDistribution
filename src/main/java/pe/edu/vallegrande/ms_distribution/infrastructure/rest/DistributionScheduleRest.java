package pe.edu.vallegrande.ms_distribution.infrastructure.rest;

import lombok.AllArgsConstructor;
import pe.edu.vallegrande.ms_distribution.application.services.DistributionScheduleService;
import pe.edu.vallegrande.ms_distribution.domain.models.DistributionSchedule;
import pe.edu.vallegrande.ms_distribution.infrastructure.dto.ErrorMessage;
import pe.edu.vallegrande.ms_distribution.infrastructure.dto.ResponseDto;
import pe.edu.vallegrande.ms_distribution.infrastructure.dto.request.DistributionScheduleCreateRequest;
import pe.edu.vallegrande.ms_distribution.infrastructure.dto.request.DistributionScheduleUpdateRequest;
import pe.edu.vallegrande.ms_distribution.infrastructure.dto.response.DistributionScheduleResponse;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/v2/schedules")
@AllArgsConstructor
public class DistributionScheduleRest {

    private final DistributionScheduleService service;

    @GetMapping
    public Mono<ResponseDto<List<DistributionSchedule>>> getAll() {
        return service.getAll()
                .collectList()
                .map(result -> new ResponseDto<>(true, result));
    }

    @GetMapping("/active")
    public Mono<ResponseDto<List<DistributionSchedule>>> getAllActive() {
        return service.getAllActive()
                .collectList()
                .map(result -> new ResponseDto<>(true, result));
    }

    @GetMapping("/inactive")
    public Mono<ResponseDto<List<DistributionSchedule>>> getAllInactive() {
        return service.getAllInactive()
                .collectList()
                .map(result -> new ResponseDto<>(true, result));
    }

    @GetMapping("/{id}")
    public Mono<ResponseDto<DistributionSchedule>> getById(@PathVariable String id) {
        return service.getById(id)
                .map(result -> new ResponseDto<>(true, result))
                .onErrorResume(e -> Mono.just(
                        new ResponseDto<>(false,
                                new ErrorMessage(HttpStatus.NOT_FOUND.value(),
                                        "Schedule not found",
                                        e.getMessage()))));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseDto<DistributionScheduleResponse>> create(@RequestBody DistributionScheduleCreateRequest request) {
        return service.save(request)
                .map(saved -> new ResponseDto<>(true, saved))
                .onErrorResume(e -> Mono.just(
                        new ResponseDto<>(false,
                                new ErrorMessage(HttpStatus.BAD_REQUEST.value(),
                                        "Validation error",
                                        e.getMessage()))));
    }

    @PutMapping("/{id}")
    public Mono<ResponseDto<DistributionSchedule>> update(@PathVariable String id, @Valid @RequestBody DistributionScheduleUpdateRequest request) {
        return service.update(id, request)
            .map(updated -> new ResponseDto<>(true, updated))
            .onErrorResume(e -> Mono.just(
                    new ResponseDto<>(false,
                            new ErrorMessage(HttpStatus.BAD_REQUEST.value(),
                                    "Update failed",
                                    e.getMessage()))));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<ResponseDto<Object>> delete(@PathVariable String id) {
        return service.delete(id)
                .thenReturn(new ResponseDto<>(true, null))
                .onErrorResume(e -> Mono.just(
                        new ResponseDto<>(false,
                                new ErrorMessage(HttpStatus.BAD_REQUEST.value(),
                                        "Delete failed",
                                        e.getMessage()))));
    }

    @PatchMapping("/{id}/activate")
    public Mono<ResponseDto<DistributionSchedule>> activate(@PathVariable String id) {
        return service.activate(id)
                .map(updated -> new ResponseDto<>(true, updated))
                .onErrorResume(e -> Mono.just(
                        new ResponseDto<>(false,
                                new ErrorMessage(HttpStatus.BAD_REQUEST.value(),
                                        "Activation failed",
                                        e.getMessage()))));
    }

    @PatchMapping("/{id}/deactivate")
    public Mono<ResponseDto<DistributionSchedule>> deactivate(@PathVariable String id) {
        return service.deactivate(id)
                .map(updated -> new ResponseDto<>(true, updated))
                .onErrorResume(e -> Mono.just(
                        new ResponseDto<>(false,
                                new ErrorMessage(HttpStatus.BAD_REQUEST.value(),
                                        "Deactivation failed",
                                        e.getMessage()))));
    }
}
