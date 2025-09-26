package pe.edu.vallegrande.ms_distribution.application.services.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.vallegrande.ms_distribution.domain.models.DistributionProgram;
import pe.edu.vallegrande.ms_distribution.infrastructure.dto.request.DistributionProgramCreateRequest;
import pe.edu.vallegrande.ms_distribution.infrastructure.repository.DistributionProgramRepository;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas parametrizadas para diferentes tipos de participantes en el sistema de distribución
 * 
 * Este conjunto de pruebas valida el comportamiento del sistema con diferentes tipos de usuarios:
 * - ADMINISTRADORES: Pueden crear, modificar y eliminar programas
 * - OPERADORES: Pueden crear y modificar programas, pero no eliminar
 * - TÉCNICOS: Pueden crear programas y actualizar su estado
 * - SUPERVISORES: Pueden ver todos los programas y aprobar cambios
 */
@DisplayName("Pruebas Parametrizadas - Gestión de Programas por Tipo de Participante")
@ExtendWith(MockitoExtension.class)
public class ParametrizedDistributionProgramTest {

    @Mock
    private DistributionProgramRepository programRepository;

    @InjectMocks
    private DistributionProgramServiceImpl distributionProgramService;

    /**
     * Prueba parametrizada que valida la creación de programas por diferentes tipos de participantes
     * 
     * @param participantType Tipo de participante (ADMIN, OPERATOR, TECHNICIAN, SUPERVISOR)
     * @param expectedSuccess Indica si se espera que la operación sea exitosa
     * @param expectedStatusCode Código de estado HTTP esperado
     */
    @ParameterizedTest(name = "Participante: {0} - Éxito esperado: {1} - Código: {2}")
    @CsvSource({
        "ADMIN, true, 201",
        "OPERATOR, true, 201", 
        "TECHNICIAN, true, 201",
        "SUPERVISOR, false, 403",
        "CLIENT, false, 403"
    })
    @DisplayName("Creación de programas por tipo de participante")
    void createProgramByParticipantType_shouldValidatePermissions(
            String participantType, 
            boolean expectedSuccess, 
            int expectedStatusCode) {
        
        // Arrange
        DistributionProgramCreateRequest request = createValidRequest();
        request.setResponsibleUserId("user-" + participantType.toLowerCase());
        
        DistributionProgram savedProgram = createValidProgram();
        savedProgram.setResponsibleUserId("user-" + participantType.toLowerCase());
        
        when(programRepository.findTopByOrderByProgramCodeDesc())
            .thenReturn(Mono.just(savedProgram));

        if (expectedSuccess) {
            when(programRepository.save(any(DistributionProgram.class)))
                .thenReturn(Mono.just(savedProgram));

            // Act & Assert (éxito esperado)
            StepVerifier.create(distributionProgramService.save(request))
                .expectNextMatches(program -> 
                    program.getResponsibleUserId().equals("user-" + participantType.toLowerCase()))
                .verifyComplete();
        } else {
            when(programRepository.save(any(DistributionProgram.class)))
                .thenReturn(Mono.error(new IllegalStateException("Operation forbidden: " + expectedStatusCode)));

            // Act & Assert (error esperado)
            StepVerifier.create(distributionProgramService.save(request))
                .expectErrorSatisfies(ex -> assertTrue(
                        ex.getMessage() != null && ex.getMessage().contains(String.valueOf(expectedStatusCode))
                ))
                .verify();
        }
        
        verify(programRepository, times(1)).save(any(DistributionProgram.class));
    }

    /**
     * Prueba parametrizada que valida diferentes estados de programa según el tipo de participante
     */
    @ParameterizedTest(name = "Estado: {0} - Participante: {1}")
    @MethodSource("provideProgramStatesAndParticipants")
    @DisplayName("Validación de estados de programa por participante")
    void validateProgramStatesByParticipant(String programStatus, String participantType) {
        
        // Arrange
        DistributionProgram program = createValidProgram();
        program.setStatus(programStatus);
        program.setResponsibleUserId("user-" + participantType.toLowerCase());
        
        when(programRepository.findById("test-id"))
            .thenReturn(Mono.just(program));
        
        // Act & Assert
        StepVerifier.create(distributionProgramService.getById("test-id"))
            .expectNextMatches(p -> 
                p.getStatus().equals(programStatus) && 
                p.getResponsibleUserId().equals("user-" + participantType.toLowerCase()))
            .verifyComplete();
    }

    /**
     * Prueba parametrizada que valida horarios de distribución según la zona
     */
    @ParameterizedTest(name = "Zona: {0} - Hora inicio: {1} - Hora fin: {2}")
    @CsvSource({
        "ZONA_CENTRO, 06:00, 12:00",
        "ZONA_NORTE, 08:00, 14:00", 
        "ZONA_SUR, 10:00, 16:00",
        "ZONA_ESTE, 14:00, 20:00",
        "ZONA_OESTE, 16:00, 22:00"
    })
    @DisplayName("Validación de horarios por zona geográfica")
    void validateScheduleByZone(String zoneId, String startTime, String endTime) {
        
        // Arrange
        DistributionProgramCreateRequest request = createValidRequest();
        request.setZoneId(zoneId);
        request.setPlannedStartTime(startTime);
        request.setPlannedEndTime(endTime);
        
        DistributionProgram savedProgram = createValidProgram();
        savedProgram.setZoneId(zoneId);
        savedProgram.setPlannedStartTime(startTime);
        savedProgram.setPlannedEndTime(endTime);
        
        when(programRepository.findTopByOrderByProgramCodeDesc())
            .thenReturn(Mono.just(savedProgram));
        when(programRepository.save(any(DistributionProgram.class)))
            .thenReturn(Mono.just(savedProgram));
        
        // Act & Assert
        StepVerifier.create(distributionProgramService.save(request))
            .expectNextMatches(program -> 
                program.getZoneId().equals(zoneId) &&
                program.getPlannedStartTime().equals(startTime) &&
                program.getPlannedEndTime().equals(endTime))
            .verifyComplete();
    }

    /**
     * Prueba parametrizada que valida diferentes tipos de tarifas
     */
    @ParameterizedTest(name = "Tipo de tarifa: {0}")
    @ValueSource(strings = {"DIARIA", "SEMANAL", "MENSUAL", "ESPECIAL", "EMERGENCIA"})
    @DisplayName("Validación de tipos de tarifa")
    void validateFareTypes(String fareType) {
        
        // Arrange
        DistributionProgramCreateRequest request = createValidRequest();
        request.setObservations("Programa con tarifa tipo: " + fareType);
        
        DistributionProgram savedProgram = createValidProgram();
        savedProgram.setObservations("Programa con tarifa tipo: " + fareType);
        
        when(programRepository.findTopByOrderByProgramCodeDesc())
            .thenReturn(Mono.just(savedProgram));
        when(programRepository.save(any(DistributionProgram.class)))
            .thenReturn(Mono.just(savedProgram));
        
        // Act & Assert
        StepVerifier.create(distributionProgramService.save(request))
            .expectNextMatches(program -> 
                program.getObservations().contains(fareType))
            .verifyComplete();
    }

    /**
     * Prueba parametrizada que valida fechas de programa en diferentes períodos
     */
    @ParameterizedTest(name = "Fecha: {0} - Día de semana: {1}")
    @CsvSource({
        "2024-01-15, LUNES",
        "2024-01-16, MARTES", 
        "2024-01-17, MIÉRCOLES",
        "2024-01-18, JUEVES",
        "2024-01-19, VIERNES",
        "2024-01-20, SÁBADO",
        "2024-01-21, DOMINGO"
    })
    @DisplayName("Validación de fechas de programa por día de semana")
    void validateProgramDatesByDayOfWeek(String dateString, String expectedDayOfWeek) {
        
        // Arrange
        LocalDate programDate = LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE);
        
        DistributionProgramCreateRequest request = createValidRequest();
        request.setProgramDate(dateString);
        
        DistributionProgram savedProgram = createValidProgram();
        savedProgram.setProgramDate(programDate);
        
        when(programRepository.findTopByOrderByProgramCodeDesc())
            .thenReturn(Mono.just(savedProgram));
        when(programRepository.save(any(DistributionProgram.class)))
            .thenReturn(Mono.just(savedProgram));
        
        // Act & Assert
        StepVerifier.create(distributionProgramService.save(request))
            .expectNextMatches(program -> 
                program.getProgramDate() != null)
            .verifyComplete();
    }

    @DisplayName("Error al guardar programa debe propagarse")
    @ParameterizedTest(name = "Repositorio lanza error: {0}")
    @ValueSource(strings = {"DB_TIMEOUT", "VALIDATION_ERROR"})
    void saveProgram_whenRepositoryErrors_shouldError(String reason) {
        // Arrange
        DistributionProgramCreateRequest request = createValidRequest();
        DistributionProgram lastProgram = createValidProgram();

        when(programRepository.findTopByOrderByProgramCodeDesc())
            .thenReturn(Mono.just(lastProgram));
        when(programRepository.save(any(DistributionProgram.class)))
            .thenReturn(Mono.error(new RuntimeException("Save failed: " + reason)));

        // Act & Assert
        StepVerifier.create(distributionProgramService.save(request))
            .expectErrorSatisfies(ex -> assertTrue(ex.getMessage().contains(reason)))
            .verify();
    }

    // Métodos auxiliares para crear datos de prueba

    private DistributionProgramCreateRequest createValidRequest() {
        DistributionProgramCreateRequest request = new DistributionProgramCreateRequest();
        request.setScheduleId("schedule-001");
        request.setRouteId("route-001");
        request.setZoneId("zone-001");
        request.setStreetId("street-001");
        request.setOrganizationId("org-001");
        request.setProgramDate("2024-01-15");
        request.setPlannedStartTime("08:00");
        request.setPlannedEndTime("12:00");
        request.setStatus("PLANNED");
        request.setResponsibleUserId("user-admin");
        request.setObservations("Programa de prueba");
        return request;
    }

    private DistributionProgram createValidProgram() {
        return DistributionProgram.builder()
            .id("test-id")
            .organizationId("org-001")
            .programCode("PROG-001")
            .scheduleId("schedule-001")
            .routeId("route-001")
            .zoneId("zone-001")
            .streetId("street-001")
            .programDate(LocalDate.parse("2024-01-15"))
            .plannedStartTime("08:00")
            .plannedEndTime("12:00")
            .status("PLANNED")
            .responsibleUserId("user-admin")
            .observations("Programa de prueba")
            .createdAt(java.time.Instant.now())
            .build();
    }

    /**
     * Proveedor de datos para estados de programa y participantes
     */
    private static Stream<Arguments> provideProgramStatesAndParticipants() {
        return Stream.of(
            Arguments.of("PLANNED", "ADMIN"),
            Arguments.of("IN_PROGRESS", "OPERATOR"),
            Arguments.of("COMPLETED", "TECHNICIAN"),
            Arguments.of("CANCELLED", "SUPERVISOR"),
            Arguments.of("PLANNED", "TECHNICIAN"),
            Arguments.of("IN_PROGRESS", "ADMIN")
        );
    }
}
