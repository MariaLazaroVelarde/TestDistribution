package pe.edu.vallegrande.ms_distribution.application.services.impl;

import lombok.RequiredArgsConstructor;
import pe.edu.vallegrande.ms_distribution.application.services.DistributionProgramService;
import pe.edu.vallegrande.ms_distribution.domain.enums.Constants;
import pe.edu.vallegrande.ms_distribution.domain.models.DistributionProgram;
import pe.edu.vallegrande.ms_distribution.infrastructure.dto.request.DistributionProgramCreateRequest;
import pe.edu.vallegrande.ms_distribution.infrastructure.dto.response.DistributionProgramResponse;
import pe.edu.vallegrande.ms_distribution.infrastructure.exception.CustomException;
import pe.edu.vallegrande.ms_distribution.infrastructure.repository.DistributionProgramRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class DistributionProgramServiceImpl implements DistributionProgramService {

    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String PROGRAM_PREFIX = "PROG";
    private static final String PROGRAM_NOT_FOUND_MESSAGE = "Program with ID %s not found";
    
    private final DistributionProgramRepository programRepository;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN);

    @Override
    public Flux<DistributionProgramResponse> getAll() {
        return programRepository.findAll()
                .map(this::toResponse);
    }

    @Override
    public Mono<DistributionProgramResponse> getById(String id) {
        return programRepository.findById(id)
                .switchIfEmpty(Mono.error(new CustomException(
                        HttpStatus.NOT_FOUND.value(), "Not found", String.format(PROGRAM_NOT_FOUND_MESSAGE, id))))
                .map(this::toResponse);
    }

    @Override
    public Mono<DistributionProgramResponse> save(DistributionProgramCreateRequest request) {
        return generateNextProgramCode()
                .flatMap(generatedCode -> {
                    DistributionProgram program = DistributionProgram.builder()
                            .programCode(generatedCode)
                            .scheduleId(request.getScheduleId())
                            .routeId(request.getRouteId())
                            .zoneId(request.getZoneId())
                            .organizationId(request.getOrganizationId())
                            .streetId(request.getStreetId())
                            .programDate(LocalDate.parse(request.getProgramDate(), dateFormatter))
                            .plannedStartTime(request.getPlannedStartTime())
                            .plannedEndTime(request.getPlannedEndTime())
                            .actualStartTime(request.getActualStartTime())
                            .actualEndTime(request.getActualEndTime())
                            .status(request.getStatus())
                            .responsibleUserId(request.getResponsibleUserId())
                            .observations(request.getObservations())
                            .createdAt(Instant.now())
                            .build();

                    return programRepository.save(program)
                            .map(this::toResponse);
                });
    }

    private Mono<String> generateNextProgramCode() {
        return programRepository.findTopByOrderByProgramCodeDesc()
                .map(this::extractNextProgramCode)
                .defaultIfEmpty(PROGRAM_PREFIX + "001");
    }
    
    private String extractNextProgramCode(DistributionProgram lastProgram) {
        String lastCode = lastProgram.getProgramCode();
        int number = 0;
        try {
            String numericPart = lastCode.replace(PROGRAM_PREFIX, "");
            number = Integer.parseInt(numericPart);
        } catch (NumberFormatException e) {
            // Si no es numérico, empezar desde 1
            number = 0;
        }
        return String.format("%s%03d", PROGRAM_PREFIX, number + 1);
    }

    @Override
    public Mono<DistributionProgramResponse> update(String id, DistributionProgramCreateRequest request) {
        return programRepository.findById(id)
                .switchIfEmpty(Mono.error(new CustomException(
                        HttpStatus.NOT_FOUND.value(), "Not found", String.format(PROGRAM_NOT_FOUND_MESSAGE, id))))
                .flatMap(existing -> updateProgramFields(existing, request))
                .map(this::toResponse);
    }
    
    private Mono<DistributionProgram> updateProgramFields(DistributionProgram existing, DistributionProgramCreateRequest request) {
        existing.setOrganizationId(request.getOrganizationId());
        existing.setZoneId(request.getZoneId());
        existing.setStreetId(request.getStreetId());
        existing.setPlannedStartTime(request.getPlannedStartTime());
        existing.setPlannedEndTime(request.getPlannedEndTime());
        existing.setActualStartTime(request.getActualStartTime());
        existing.setActualEndTime(request.getActualEndTime());
        existing.setStatus(request.getStatus());
        existing.setObservations(request.getObservations());
        existing.setResponsibleUserId(request.getResponsibleUserId());
        return programRepository.save(existing);
    }

    @Override
    public Mono<Void> delete(String id) {
        return programRepository.findById(id)
                .switchIfEmpty(Mono.error(new CustomException(
                        HttpStatus.NOT_FOUND.value(), "Not found", String.format(PROGRAM_NOT_FOUND_MESSAGE, id))))
                .flatMap(programRepository::delete);
    }

    @Override
    public Mono<DistributionProgramResponse> activate(String id) {
        return changeStatus(id, Constants.ACTIVE.name());
    }

    @Override
    public Mono<DistributionProgramResponse> desactivate(String id) {
        return changeStatus(id, Constants.INACTIVE.name());
    }

    @Override
    public Mono<DistributionProgramResponse> changeStatus(String id, String status) {
        return programRepository.findById(id)
                .switchIfEmpty(Mono.error(new CustomException(
                        HttpStatus.NOT_FOUND.value(), "Not found", String.format(PROGRAM_NOT_FOUND_MESSAGE, id))))
                .flatMap(program -> updateProgramStatus(program, status))
                .map(this::toResponse);
    }
    
    private Mono<DistributionProgram> updateProgramStatus(DistributionProgram program, String status) {
        program.setStatus(status);
        return programRepository.save(program);
    }

    // Mapeo de entidad a DTO
    private DistributionProgramResponse toResponse(DistributionProgram program) {
        return DistributionProgramResponse.builder()
                .id(program.getId())
                .organizationId(program.getOrganizationId())
                .zoneId(program.getZoneId())
                .streetId(program.getStreetId())
                .programCode(program.getProgramCode())
                .scheduleId(program.getScheduleId())
                .routeId(program.getRouteId())
                .programDate(program.getProgramDate() != null ? program.getProgramDate().format(dateFormatter) : null)
                .plannedStartTime(program.getPlannedStartTime())
                .plannedEndTime(program.getPlannedEndTime())
                .actualStartTime(program.getActualStartTime())
                .actualEndTime(program.getActualEndTime())
                .status(program.getStatus())
                .responsibleUserId(program.getResponsibleUserId())
                .observations(program.getObservations())
                .createdAt(program.getCreatedAt() != null ? program.getCreatedAt().toString() : null)
                .build();
    }
}
