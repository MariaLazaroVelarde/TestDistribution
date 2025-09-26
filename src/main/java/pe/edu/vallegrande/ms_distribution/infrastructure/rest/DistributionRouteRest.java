package pe.edu.vallegrande.ms_distribution.infrastructure.rest;

import lombok.AllArgsConstructor;
import pe.edu.vallegrande.ms_distribution.application.services.DistributionRouteService;
import pe.edu.vallegrande.ms_distribution.domain.models.DistributionRoute;
import pe.edu.vallegrande.ms_distribution.infrastructure.dto.ErrorMessage;
import pe.edu.vallegrande.ms_distribution.infrastructure.dto.ResponseDto;
import pe.edu.vallegrande.ms_distribution.infrastructure.dto.request.DistributionRouteCreateRequest;
import pe.edu.vallegrande.ms_distribution.infrastructure.dto.request.DistributionRouteUpdateRequest;
import pe.edu.vallegrande.ms_distribution.infrastructure.dto.response.DistributionRouteResponse;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/v2/routes")
@AllArgsConstructor
public class DistributionRouteRest {

    private final DistributionRouteService routeService;

    @GetMapping
    public Mono<ResponseDto<List<DistributionRoute>>> getAll() {
        return routeService.getAll()
                .collectList()
                .map(routes -> new ResponseDto<>(true, routes));
    }

    @GetMapping("/active")
    public Mono<ResponseDto<List<DistributionRoute>>> getAllActive() {
        return routeService.getAllActive()
                .collectList()
                .map(routes -> new ResponseDto<>(true, routes));
    }

    @GetMapping("/inactive")
    public Mono<ResponseDto<List<DistributionRoute>>> getAllInactive() {
        return routeService.getAllInactive()
                .collectList()
                .map(routes -> new ResponseDto<>(true, routes));
    }

    @GetMapping("/{id}")
    public Mono<ResponseDto<DistributionRoute>> getById(@PathVariable String id) {
        return routeService.getById(id)
                .map(route -> new ResponseDto<>(true, route))
                .onErrorResume(e -> Mono.just(new ResponseDto<>(false,
                        new ErrorMessage(HttpStatus.NOT_FOUND.value(), "Route not found", e.getMessage()))));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseDto<DistributionRouteResponse>> create(@RequestBody DistributionRouteCreateRequest request) {
        return routeService.save(request)
                .map(saved -> new ResponseDto<>(true, saved))
                .onErrorResume(e -> Mono.just(new ResponseDto<>(false,
                        new ErrorMessage(HttpStatus.BAD_REQUEST.value(), "Validation error", e.getMessage()))));
    }

    @PutMapping("/{id}")
    public Mono<ResponseDto<DistributionRoute>> update(@PathVariable String id, @Valid @RequestBody DistributionRouteUpdateRequest request) {
        return routeService.update(id, request)
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
        return routeService.delete(id)
                .then(Mono.just(new ResponseDto<>(true, null)))
                .onErrorResume(e -> Mono.just(new ResponseDto<>(false,
                        new ErrorMessage(HttpStatus.BAD_REQUEST.value(), "Delete failed", e.getMessage()))));
    }

    @PatchMapping("/{id}/activate")
    public Mono<ResponseDto<DistributionRoute>> activate(@PathVariable String id) {
        return routeService.activate(id)
                .map(route -> new ResponseDto<>(true, route))
                .onErrorResume(e -> Mono.just(new ResponseDto<>(false,
                        new ErrorMessage(HttpStatus.BAD_REQUEST.value(), "Activation failed", e.getMessage()))));
    }

    @PatchMapping("/{id}/deactivate")
    public Mono<ResponseDto<DistributionRoute>> deactivate(@PathVariable String id) {
        return routeService.deactivate(id)
                .map(route -> new ResponseDto<>(true, route))
                .onErrorResume(e -> Mono.just(new ResponseDto<>(false,
                        new ErrorMessage(HttpStatus.BAD_REQUEST.value(), "Deactivation failed", e.getMessage()))));
    }
}
