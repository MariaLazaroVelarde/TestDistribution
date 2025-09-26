package pe.edu.vallegrande.ms_distribution.infrastructure.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import pe.edu.vallegrande.ms_distribution.domain.models.DistributionRoute;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface DistributionRouteRepository extends ReactiveMongoRepository<DistributionRoute, String> {

    Flux<DistributionRoute> findAllByStatus(String status);

    Mono<Boolean> existsByRouteCode(String routeCode);

    Mono<DistributionRoute> findTopByOrderByRouteCodeDesc();
}
