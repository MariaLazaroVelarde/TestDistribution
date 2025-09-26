package pe.edu.vallegrande.ms_distribution.application.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.ms_distribution.application.services.FareService;
import pe.edu.vallegrande.ms_distribution.domain.enums.Constants;
import pe.edu.vallegrande.ms_distribution.domain.models.Fare;
import pe.edu.vallegrande.ms_distribution.infrastructure.dto.request.FareCreateRequest;
import pe.edu.vallegrande.ms_distribution.infrastructure.dto.request.FareUpdateRequest;
import pe.edu.vallegrande.ms_distribution.infrastructure.dto.response.FareResponse;
import pe.edu.vallegrande.ms_distribution.infrastructure.exception.CustomException;
import pe.edu.vallegrande.ms_distribution.infrastructure.repository.FareRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.math.BigDecimal;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class FareServiceImpl implements FareService {

    private final FareRepository fareRepository;
    private static final String FARE_PREFIX = "TAR";
    private static final String INITIAL_FARE_CODE = FARE_PREFIX + "001";

    @Override
    public Flux<Fare> getAllF() {
        return fareRepository.findAll()
                .doOnNext(f -> log.debug("Fare retrieved: {}", f))
                .doOnError(error -> log.error("Error retrieving all fares: {}", error.getMessage()));
    }

    @Override
    public Flux<Fare> getAllActiveF() {
        return fareRepository.findAllByStatus(Constants.ACTIVE.name())
                .doOnError(error -> log.error("Error retrieving active fares: {}", error.getMessage()));
    }

    @Override
    public Flux<Fare> getAllInactiveF() {
        return fareRepository.findAllByStatus(Constants.INACTIVE.name())
                .doOnError(error -> log.error("Error retrieving inactive fares: {}", error.getMessage()));
    }

    @Override
    public Mono<Fare> getByIdFMono(String id) {
        validateId(id);
        return fareRepository.findById(id)
                .switchIfEmpty(Mono.error(createFareNotFoundError(id)))
                .doOnSuccess(fare -> log.debug("Fare found with id: {}", id))
                .doOnError(error -> log.error("Error finding fare with id {}: {}", id, error.getMessage()));
    }

    @Override
    public Mono<FareResponse> saveF(FareCreateRequest request) {
        validateCreateRequest(request);
        
        return generateNextFareCode()
                .flatMap(code -> validateFareCodeNotExists(code)
                        .then(createAndSaveFare(request, code)))
                .doOnSuccess(response -> log.info("Fare created successfully with code: {}", response.getFareCode()))
                .doOnError(error -> log.error("Error creating fare: {}", error.getMessage()));
    }

    private Mono<String> generateNextFareCode() {
        return fareRepository.findTopByOrderByFareCodeDesc()
                .map(this::extractNextCodeFromLastFare)
                .defaultIfEmpty(INITIAL_FARE_CODE)
                .doOnNext(code -> log.debug("Generated fare code: {}", code));
    }

    private String extractNextCodeFromLastFare(Fare lastFare) {
        try {
            String lastCode = lastFare.getFareCode();
            if (lastCode == null || !lastCode.startsWith(FARE_PREFIX)) {
                log.warn("Invalid fare code format: {}, using default", lastCode);
                return INITIAL_FARE_CODE;
            }
            
            String numericPart = lastCode.replace(FARE_PREFIX, "");
            int number = parseNumericCode(numericPart);
            return String.format(FARE_PREFIX + "%03d", number + 1);
        } catch (Exception e) {
            log.warn("Error extracting code from last fare, using default: {}", e.getMessage());
            return INITIAL_FARE_CODE;
        }
    }

    private int parseNumericCode(String numericPart) {
        if (numericPart == null || numericPart.trim().isEmpty()) {
            return 0;
        }
        
        if (!numericPart.matches("\\d+")) {
            log.warn("Invalid numeric code format: {}, using 0", numericPart);
            return 0;
        }
        
        try {
            return Integer.parseInt(numericPart);
        } catch (NumberFormatException e) {
            log.warn("Cannot parse numeric code: {}, using 0", numericPart);
            return 0;
        }
    }

    private Mono<Void> validateFareCodeNotExists(String fareCode) {
        return fareRepository.existsByFareCode(fareCode)
                .flatMap(exists -> {
                    if (exists) {
                        log.warn("Attempt to create fare with existing code: {}", fareCode);
                        return Mono.error(createFareCodeExistsError(fareCode));
                    }
                    return Mono.empty();
                });
    }

    private Mono<FareResponse> createAndSaveFare(FareCreateRequest request, String fareCode) {
        Fare fare = buildFareFromRequest(request, fareCode);
        return fareRepository.save(fare)
                .map(this::mapToFareResponse)
                .doOnSuccess(response -> log.debug("Fare saved with id: {}", response.getId()));
    }

    private Fare buildFareFromRequest(FareCreateRequest request, String fareCode) {
        return Fare.builder()
                .organizationId(request.getOrganizationId())
                .fareCode(fareCode)
                .fareName(request.getFareName())
                .fareType(request.getFareType())
                .fareAmount(request.getFareAmount())
                .status(Constants.ACTIVE.name())
                .createdAt(Instant.now())
                .build();
    }

    private FareResponse mapToFareResponse(Fare fare) {
        return FareResponse.builder()
                .id(fare.getId())
                .organizationId(fare.getOrganizationId())
                .fareCode(fare.getFareCode())
                .fareName(fare.getFareName())
                .fareType(fare.getFareType())
                .fareAmount(fare.getFareAmount())
                .status(fare.getStatus())
                .createdAt(fare.getCreatedAt())
                .build();
    }

    @Override
    public Mono<Fare> updateF(String id, FareUpdateRequest request) {
        validateId(id);
        validateUpdateRequest(request);
        
        return fareRepository.findById(id)
                .switchIfEmpty(Mono.error(createFareNotFoundError(id)))
                .flatMap(existing -> updateFareFields(existing, request))
                .flatMap(fareRepository::save)
                .doOnSuccess(updated -> log.info("Fare updated successfully: {}", id))
                .doOnError(error -> log.error("Error updating fare {}: {}", id, error.getMessage()));
    }

    private Mono<Fare> updateFareFields(Fare existing, FareUpdateRequest request) {
        if (request.getFareCode() != null) {
            existing.setFareCode(request.getFareCode());
        }
        if (request.getPrice() != null) {
            existing.setFareAmount(BigDecimal.valueOf(request.getPrice()));
        }
        // request.getDescription() is ignored because Fare model has no description field
        return Mono.just(existing);
    }

    @Override
    public Mono<Void> deleteF(String id) {
        validateId(id);
        
        return fareRepository.findById(id)
                .switchIfEmpty(Mono.error(createFareNotFoundError(id, "Cannot delete non-existent fare")))
                .flatMap(fare -> {
                    log.info("Deleting fare: {}", id);
                    return fareRepository.delete(fare);
                })
                .doOnSuccess(v -> log.info("Fare deleted successfully: {}", id))
                .doOnError(error -> log.error("Error deleting fare {}: {}", id, error.getMessage()));
    }

    @Override
    public Mono<Fare> activateF(String id) {
        return changeStatus(id, Constants.ACTIVE.name());
    }

    @Override
    public Mono<Fare> deactivateF(String id) {
        return changeStatus(id, Constants.INACTIVE.name());
    }

    private Mono<Fare> changeStatus(String id, String newStatus) {
        validateId(id);
        
        return fareRepository.findById(id)
                .switchIfEmpty(Mono.error(CustomException.notFound("Fare", id)))
                .flatMap(fare -> {
                    String oldStatus = fare.getStatus();
                    if (Objects.equals(oldStatus, newStatus)) {
                        log.debug("Fare {} already has status {}", id, newStatus);
                        return Mono.just(fare);
                    }
                    
                    fare.setStatus(newStatus);
                    log.info("Changing fare {} status from {} to {}", id, oldStatus, newStatus);
                    
                    return fareRepository.save(fare)
                            .doOnSuccess(saved -> log.debug("Status change saved for fare: {}", id));
                })
                .doOnError(error -> log.error("Error changing status for fare {}: {}", id, error.getMessage()));
    }

    // Métodos de validación privados
    private void validateId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Fare ID cannot be null or empty");
        }
    }

    private void validateCreateRequest(FareCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("FareCreateRequest cannot be null");
        }
        if (request.getOrganizationId() == null || request.getOrganizationId().trim().isEmpty()) {
            throw new IllegalArgumentException("Organization ID is required");
        }
        if (request.getFareName() == null || request.getFareName().trim().isEmpty()) {
            throw new IllegalArgumentException("Fare name is required");
        }
        if (request.getFareAmount() == null || request.getFareAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Fare amount must be greater than zero");
        }
    }

    private void validateUpdateRequest(FareUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("FareUpdateRequest cannot be null");
        }
    }

    // Métodos de creación de errores privados
    private CustomException createFareNotFoundError(String id) {
        return new CustomException(
                HttpStatus.NOT_FOUND.value(),
                "Fare not found",
                "The requested fare with id " + id + " was not found"
        );
    }

    private CustomException createFareNotFoundError(String id, String action) {
        return new CustomException(
                HttpStatus.NOT_FOUND.value(),
                "Fare not found",
                action + " with id " + id
        );
    }

    private CustomException createFareCodeExistsError(String fareCode) {
        return new CustomException(
                HttpStatus.BAD_REQUEST.value(),
                "Fare code already exists",
                "The fare code " + fareCode + " is already registered"
        );
    }
}