package pe.edu.vallegrande.ms_distribution.application.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pe.edu.vallegrande.ms_distribution.application.services.DistributionRouteService;
import pe.edu.vallegrande.ms_distribution.domain.enums.Constants;
import pe.edu.vallegrande.ms_distribution.domain.models.DistributionRoute;
import pe.edu.vallegrande.ms_distribution.infrastructure.dto.request.DistributionRouteCreateRequest;
import pe.edu.vallegrande.ms_distribution.infrastructure.dto.request.DistributionRouteUpdateRequest;
import pe.edu.vallegrande.ms_distribution.infrastructure.dto.response.DistributionRouteResponse;
import pe.edu.vallegrande.ms_distribution.infrastructure.exception.CustomException;
import pe.edu.vallegrande.ms_distribution.infrastructure.repository.DistributionRouteRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
public class DistributionRouteServiceImpl implements DistributionRouteService {

    private final DistributionRouteRepository routeRepository;

    @Override
    public Flux<DistributionRoute> getAll() {
        return routeRepository.findAll();
    }

    @Override
    public Flux<DistributionRoute> getAllActive() {
        return routeRepository.findAllByStatus(Constants.ACTIVE.name());
    }

    @Override
    public Flux<DistributionRoute> getAllInactive() {
        return routeRepository.findAllByStatus(Constants.INACTIVE.name());
    }

    @Override
    public Mono<DistributionRoute> getById(String id) {
        return routeRepository.findById(id)
                .switchIfEmpty(Mono.error(new CustomException(
                        HttpStatus.NOT_FOUND.value(), "Route not found", "Route with ID " + id + " does not exist")));
    }

    @Override
    public Mono<DistributionRouteResponse> save(DistributionRouteCreateRequest request) {
        return generateNextRouteCode()
            .flatMap(generatedCode -> {

                // 👇 Convertimos ZoneEntry a ZoneOrder
                List<DistributionRoute.ZoneOrder> zoneOrders = request.getZones().stream()
                        .map(entry -> DistributionRoute.ZoneOrder.builder()
                                .zoneId(entry.getZoneId())
                                .order(entry.getOrder())
                                .estimatedDuration(entry.getEstimatedDuration())
                                .build())
                        .collect(Collectors.toList());

                DistributionRoute route = DistributionRoute.builder()
                        .organizationId(request.getOrganizationId())
                        .routeCode(generatedCode) // ← Usamos el código generado
                        .routeName(request.getRouteName())
                        .zones(zoneOrders)
                        .totalEstimatedDuration(request.getTotalEstimatedDuration())
                        .responsibleUserId(request.getResponsibleUserId())
                        .status(Constants.ACTIVE.name())
                        .createdAt(Instant.now())
                        .build();

                return routeRepository.save(route)
                        .map(saved -> DistributionRouteResponse.builder()
                                .id(saved.getId())
                                .organizationId(saved.getOrganizationId())
                                .routeCode(saved.getRouteCode())
                                .routeName(saved.getRouteName())
                                .zones(
                                        saved.getZones().stream()
                                                .map(z -> DistributionRouteResponse.ZoneDetail.builder()
                                                        .zoneId(z.getZoneId())
                                                        .order(z.getOrder())
                                                        .estimatedDuration(z.getEstimatedDuration())
                                                        .build())
                                                .collect(Collectors.toList())
                                )
                                .totalEstimatedDuration(saved.getTotalEstimatedDuration())
                                .responsibleUserId(saved.getResponsibleUserId())
                                .status(saved.getStatus())
                                .createdAt(saved.getCreatedAt())
                                .build());
            });
    }

    private Mono<String> generateNextRouteCode() {
        return routeRepository.findTopByOrderByRouteCodeDesc()
                .map(last -> {
                    String lastCode = last.getRouteCode(); // Ej: "RUT005"
                    int number = Integer.parseInt(lastCode.replace("RUT", ""));
                    return String.format("RUT%03d", number + 1);
                })
                .defaultIfEmpty("RUT001");
    }


    @Override
    public Mono<DistributionRoute> update(String id, DistributionRouteUpdateRequest request) {
        return routeRepository.findById(id)
                .flatMap(existingRoute -> {
                    existingRoute.setRouteName(request.getRouteName());
                    existingRoute.setTotalEstimatedDuration(request.getTotalEstimatedDuration());
                    existingRoute.setResponsibleUserId(request.getResponsibleUserId());
                    // Si es necesario convertir las zonas del DTO a las zonas de la entidad:
                    existingRoute.setZones(
                            request.getZones().stream()
                                .map(z -> new DistributionRoute.ZoneOrder(
                                        z.getZoneId(),
                                        z.getOrder(),
                                        z.getEstimatedDuration()))
                                .toList()
                    );
                    return routeRepository.save(existingRoute);
                });
    }

    @Override
    public Mono<Void> delete(String id) {
        return routeRepository.findById(id)
                .switchIfEmpty(Mono.error(new CustomException(
                        HttpStatus.NOT_FOUND.value(), "Route not found", "Cannot delete non-existent route with id " + id)))
                .flatMap(routeRepository::delete);
    }

    @Override
    public Mono<DistributionRoute> activate(String id) {
        return changeStatus(id, Constants.ACTIVE.name());
    }

    @Override
    public Mono<DistributionRoute> deactivate(String id) {
        return changeStatus(id, Constants.INACTIVE.name());
    }

    private Mono<DistributionRoute> changeStatus(String id, String status) {
        return routeRepository.findById(id)
                .switchIfEmpty(Mono.error(new CustomException(
                        HttpStatus.NOT_FOUND.value(), "Route not found", "Cannot change status of non-existent route with id " + id)))
                .flatMap(route -> {
                    route.setStatus(status);
                    return routeRepository.save(route);
                });
    }
}
