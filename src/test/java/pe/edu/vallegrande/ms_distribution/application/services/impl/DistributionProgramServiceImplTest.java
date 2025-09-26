package pe.edu.vallegrande.ms_distribution.application.services.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pe.edu.vallegrande.ms_distribution.domain.models.DistributionProgram;
import pe.edu.vallegrande.ms_distribution.infrastructure.dto.request.DistributionProgramCreateRequest;
import pe.edu.vallegrande.ms_distribution.infrastructure.repository.DistributionProgramRepository;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class DistributionProgramServiceImplTest {

    @Mock
    private DistributionProgramRepository programRepository;

    @InjectMocks
    private DistributionProgramServiceImpl distributionProgramService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAll_shouldMapToResponse() {
        DistributionProgram p1 = DistributionProgram.builder()
                .id("1").organizationId("org").zoneId("z").streetId("st")
                .programCode("PROG001").scheduleId("sch").routeId("r")
                .programDate(LocalDate.parse("2024-01-01"))
                .plannedStartTime("08:00").plannedEndTime("10:00")
                .status("PENDING").responsibleUserId("u").observations("obs")
                .createdAt(java.time.Instant.parse("2024-01-01T00:00:00Z")).build();

        when(programRepository.findAll()).thenReturn(Flux.just(p1));

        StepVerifier.create(distributionProgramService.getAll())
                .assertNext(resp -> {
                    org.junit.jupiter.api.Assertions.assertEquals("1", resp.getId());
                    org.junit.jupiter.api.Assertions.assertEquals("PROG001", resp.getProgramCode());
                })
                .verifyComplete();
    }

    @Test
    void getById_shouldReturn_whenExists() {
        DistributionProgram p = DistributionProgram.builder().id("id-1").programCode("PROG007").build();
        when(programRepository.findById("id-1")).thenReturn(Mono.just(p));

        StepVerifier.create(distributionProgramService.getById("id-1"))
                .assertNext(resp -> org.junit.jupiter.api.Assertions.assertEquals("PROG007", resp.getProgramCode()))
                .verifyComplete();
    }

    @Test
    void getById_shouldError_whenNotFound() {
        when(programRepository.findById("missing")).thenReturn(Mono.empty());
        StepVerifier.create(distributionProgramService.getById("missing"))
                .expectError(pe.edu.vallegrande.ms_distribution.infrastructure.exception.CustomException.class)
                .verify();
    }

    @Test
    void save_shouldGenerateDefaultCode_whenNoPrevious() {
        DistributionProgramCreateRequest req = validRequestFor("2024-01-02");
        when(programRepository.findTopByOrderByProgramCodeDesc()).thenReturn(Mono.empty());
        when(programRepository.save(ArgumentMatchers.any(DistributionProgram.class)))
                .thenAnswer(inv -> {
                    DistributionProgram arg = inv.getArgument(0);
                    return Mono.just(DistributionProgram.builder()
                            .id("new")
                            .organizationId(arg.getOrganizationId())
                            .programCode(arg.getProgramCode())
                            .scheduleId(arg.getScheduleId())
                            .routeId(arg.getRouteId())
                            .zoneId(arg.getZoneId())
                            .streetId(arg.getStreetId())
                            .programDate(arg.getProgramDate())
                            .plannedStartTime(arg.getPlannedStartTime())
                            .plannedEndTime(arg.getPlannedEndTime())
                            .actualStartTime(arg.getActualStartTime())
                            .actualEndTime(arg.getActualEndTime())
                            .status(arg.getStatus())
                            .responsibleUserId(arg.getResponsibleUserId())
                            .observations(arg.getObservations())
                            .createdAt(arg.getCreatedAt())
                            .build());
                });

        StepVerifier.create(distributionProgramService.save(req))
                .assertNext(resp -> org.junit.jupiter.api.Assertions.assertEquals("PROG001", resp.getProgramCode()))
                .verifyComplete();
    }

    @Test
    void save_shouldGenerateNextCode_whenPreviousNumeric() {
        DistributionProgram last = DistributionProgram.builder().programCode("PROG009").build();
        DistributionProgramCreateRequest req = validRequestFor("2024-01-03");
        when(programRepository.findTopByOrderByProgramCodeDesc()).thenReturn(Mono.just(last));
        when(programRepository.save(any(DistributionProgram.class)))
                .thenAnswer(inv -> {
                    DistributionProgram arg = inv.getArgument(0);
                    return Mono.just(DistributionProgram.builder()
                            .id("id")
                            .organizationId(arg.getOrganizationId())
                            .programCode(arg.getProgramCode())
                            .scheduleId(arg.getScheduleId())
                            .routeId(arg.getRouteId())
                            .zoneId(arg.getZoneId())
                            .streetId(arg.getStreetId())
                            .programDate(arg.getProgramDate())
                            .plannedStartTime(arg.getPlannedStartTime())
                            .plannedEndTime(arg.getPlannedEndTime())
                            .actualStartTime(arg.getActualStartTime())
                            .actualEndTime(arg.getActualEndTime())
                            .status(arg.getStatus())
                            .responsibleUserId(arg.getResponsibleUserId())
                            .observations(arg.getObservations())
                            .createdAt(arg.getCreatedAt())
                            .build());
                });

        StepVerifier.create(distributionProgramService.save(req))
                .assertNext(resp -> org.junit.jupiter.api.Assertions.assertEquals("PROG010", resp.getProgramCode()))
                .verifyComplete();
    }

    @Test
    void save_shouldFallback_whenPreviousNonNumeric() {
        DistributionProgram last = DistributionProgram.builder().programCode("BAD").build();
        DistributionProgramCreateRequest req = validRequestFor("2024-01-04");
        when(programRepository.findTopByOrderByProgramCodeDesc()).thenReturn(Mono.just(last));
        when(programRepository.save(any(DistributionProgram.class)))
                .thenAnswer(inv -> {
                    DistributionProgram arg = inv.getArgument(0);
                    return Mono.just(DistributionProgram.builder()
                            .id("id")
                            .organizationId(arg.getOrganizationId())
                            .programCode(arg.getProgramCode())
                            .scheduleId(arg.getScheduleId())
                            .routeId(arg.getRouteId())
                            .zoneId(arg.getZoneId())
                            .streetId(arg.getStreetId())
                            .programDate(arg.getProgramDate())
                            .plannedStartTime(arg.getPlannedStartTime())
                            .plannedEndTime(arg.getPlannedEndTime())
                            .actualStartTime(arg.getActualStartTime())
                            .actualEndTime(arg.getActualEndTime())
                            .status(arg.getStatus())
                            .responsibleUserId(arg.getResponsibleUserId())
                            .observations(arg.getObservations())
                            .createdAt(arg.getCreatedAt())
                            .build());
                });

        StepVerifier.create(distributionProgramService.save(req))
                .assertNext(resp -> org.junit.jupiter.api.Assertions.assertEquals("PROG001", resp.getProgramCode()))
                .verifyComplete();
    }

    @Test
    void update_shouldMapAndSave_whenExists() {
        String id = "p1";
        DistributionProgram existing = DistributionProgram.builder().id(id).build();
        DistributionProgramCreateRequest req = validRequestFor("2024-01-05");
        when(programRepository.findById(id)).thenReturn(Mono.just(existing));
        when(programRepository.save(any(DistributionProgram.class)))
                .thenAnswer(inv -> Mono.just((DistributionProgram) inv.getArgument(0)));

        StepVerifier.create(distributionProgramService.update(id, req))
                .assertNext(resp -> {
                    // programDate no es actualizado por update(); verificamos otros campos mapeados
                    org.junit.jupiter.api.Assertions.assertEquals("08:00", resp.getPlannedStartTime());
                    org.junit.jupiter.api.Assertions.assertEquals("10:00", resp.getPlannedEndTime());
                })
                .verifyComplete();
    }

    @Test
    void update_shouldError_whenNotFound() {
        when(programRepository.findById("missing")).thenReturn(Mono.empty());
        StepVerifier.create(distributionProgramService.update("missing", validRequestFor("2024-01-06")))
                .expectError(pe.edu.vallegrande.ms_distribution.infrastructure.exception.CustomException.class)
                .verify();
    }

    @Test
    void delete_shouldComplete_whenExists() {
        DistributionProgram p = DistributionProgram.builder().id("p").build();
        when(programRepository.findById("p")).thenReturn(Mono.just(p));
        when(programRepository.delete(p)).thenReturn(Mono.empty());

        StepVerifier.create(distributionProgramService.delete("p")).verifyComplete();
    }

    @Test
    void delete_shouldError_whenNotFound() {
        when(programRepository.findById("missing")).thenReturn(Mono.empty());
        StepVerifier.create(distributionProgramService.delete("missing"))
                .expectError(pe.edu.vallegrande.ms_distribution.infrastructure.exception.CustomException.class)
                .verify();
    }

    @Test
    void changeStatus_activate_shouldPersist() {
        DistributionProgram p = DistributionProgram.builder().id("p").status("OLD").build();
        when(programRepository.findById("p")).thenReturn(Mono.just(p));
        when(programRepository.save(any(DistributionProgram.class)))
                .thenAnswer(inv -> Mono.just((DistributionProgram) inv.getArgument(0)));

        StepVerifier.create(distributionProgramService.activate("p"))
                .assertNext(resp -> org.junit.jupiter.api.Assertions.assertEquals(pe.edu.vallegrande.ms_distribution.domain.enums.Constants.ACTIVE.name(), resp.getStatus()))
                .verifyComplete();
    }

    @Test
    void changeStatus_deactivate_shouldPersist() {
        DistributionProgram p = DistributionProgram.builder().id("p").status("OLD").build();
        when(programRepository.findById("p")).thenReturn(Mono.just(p));
        when(programRepository.save(any(DistributionProgram.class)))
                .thenAnswer(inv -> Mono.just((DistributionProgram) inv.getArgument(0)));

        StepVerifier.create(distributionProgramService.desactivate("p"))
                .assertNext(resp -> org.junit.jupiter.api.Assertions.assertEquals(pe.edu.vallegrande.ms_distribution.domain.enums.Constants.INACTIVE.name(), resp.getStatus()))
                .verifyComplete();
    }

    @Test
    void changeStatus_shouldError_whenNotFound() {
        when(programRepository.findById("missing")).thenReturn(Mono.empty());
        StepVerifier.create(distributionProgramService.changeStatus("missing", "ANY"))
                .expectError(pe.edu.vallegrande.ms_distribution.infrastructure.exception.CustomException.class)
                .verify();
    }

    @Test
    void toResponse_shouldHandleNullProgramDateAndCreatedAt() {
        DistributionProgram p = DistributionProgram.builder()
                .id("x").programCode("PROG123")
                .programDate(null).createdAt(null)
                .build();

        when(programRepository.findById("x")).thenReturn(Mono.just(p));

        StepVerifier.create(distributionProgramService.getById("x"))
                .assertNext(resp -> {
                    org.junit.jupiter.api.Assertions.assertNull(resp.getProgramDate());
                    org.junit.jupiter.api.Assertions.assertNull(resp.getCreatedAt());
                })
                .verifyComplete();
    }

    private DistributionProgramCreateRequest validRequestFor(String date) {
        DistributionProgramCreateRequest request = new DistributionProgramCreateRequest();
        request.setScheduleId("sch-1");
        request.setRouteId("route-1");
        request.setZoneId("zone-1");
        request.setOrganizationId("org-1");
        request.setStreetId("street-1");
        request.setProgramDate(date);
        request.setPlannedStartTime("08:00");
        request.setPlannedEndTime("10:00");
        request.setActualStartTime(null);
        request.setActualEndTime(null);
        request.setStatus("PENDING");
        request.setResponsibleUserId("user-1");
        request.setObservations("Test");
        return request;
    }

    /**
     * Escenario de Validación:
     * Debe fallar cuando la fecha del programa no tiene un formato válido.
     */
    @Test
    void saveDistributionProgram_shouldReturnError_whenProgramDateIsInvalid() {
        System.out.println("➡️ Iniciando prueba: Fecha inválida en DistributionProgram");
        
        // Arrange - Construimos la solicitud
        DistributionProgramCreateRequest request = new DistributionProgramCreateRequest();
        request.setScheduleId("sch-1");
        request.setRouteId("route-1");
        request.setZoneId("zone-1");
        request.setOrganizationId("org-1");
        request.setStreetId("street-1");
        request.setProgramDate("invalid-date"); // Invalid date format
        request.setPlannedStartTime("08:00");
        request.setPlannedEndTime("10:00");
        request.setActualStartTime(null);
        request.setActualEndTime(null);
        request.setStatus("PENDING");
        request.setResponsibleUserId("user-1");
        request.setObservations("Test observation");

        // Mock: generateNextProgramCode returns "PROG001"
        when(programRepository.findTopByOrderByProgramCodeDesc()).thenReturn(Mono.empty());

        // Act & Assert - Ejecutamos el método y validamos la respuesta
        StepVerifier.create(distributionProgramService.save(request))
                .expectErrorSatisfies(throwable -> {
                    System.out.println("❌ Error capturado: " + throwable.getMessage());
                    assert throwable instanceof RuntimeException;
                    assert throwable.getMessage().contains("Text 'invalid-date' could not be parsed");
                })
                .verify();

        // Verifica que los métodos del repositorio fueron llamados correctamente
        verify(programRepository, never()).save(any(DistributionProgram.class));
        System.out.println("✔️ Prueba completada: No se guardó el programa por fecha inválida\n");
    }

    /**
     * Escenario de Excepción:
     * Debe lanzar error cuando el repositorio falla al guardar el programa.
     */
    @Test
    void saveDistributionProgram_shouldReturnError_whenRepositoryFails() {
         System.out.println("➡️ Iniciando prueba: Falla del repositorio al guardar DistributionProgram");

        // Arrange - Construimos la solicitud
        DistributionProgramCreateRequest request = new DistributionProgramCreateRequest();
        request.setScheduleId("sch-1");
        request.setRouteId("route-1");
        request.setZoneId("zone-1");
        request.setOrganizationId("org-1");
        request.setStreetId("street-1");
        request.setProgramDate(LocalDate.now().toString());
        request.setPlannedStartTime("08:00");
        request.setPlannedEndTime("10:00");
        request.setActualStartTime(null);
        request.setActualEndTime(null);
        request.setStatus("PENDING");
        request.setResponsibleUserId("user-1");
        request.setObservations("Test observation");

        when(programRepository.findTopByOrderByProgramCodeDesc()).thenReturn(Mono.empty());
        when(programRepository.save(ArgumentMatchers.any(DistributionProgram.class)))
                .thenReturn(Mono.error(new RuntimeException("Database error")));

        // Act & Assert - Ejecutamos el método y validamos la respuesta
        StepVerifier.create(distributionProgramService.save(request))
                .expectErrorSatisfies(throwable -> {
                    System.out.println("❌ Error capturado: " + throwable.getMessage());
                    assert throwable instanceof RuntimeException;
                    assert throwable.getMessage().contains("Database error");
                })
                .verify();

        System.out.println("✔️ Prueba completada: Se detectó correctamente el error del repositorio\n");
    }
}

    /**
     * mvn -Dtest=DistributionProgramServiceImplTest test
     */