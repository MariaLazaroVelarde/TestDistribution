package pe.edu.vallegrande.ms_distribution.application.services.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pe.edu.vallegrande.ms_distribution.infrastructure.dto.request.DistributionProgramCreateRequest;
import pe.edu.vallegrande.ms_distribution.infrastructure.repository.DistributionProgramRepository;
import reactor.core.publisher.Mono;
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

    /**
     * Prueba parametrizada: ejecuta la misma prueba con distintos valores
     * de responsibleUserId (user-1, user-2, user-3).
     */
    @ParameterizedTest(name = "Crear programa con participante: {0}")
    @ValueSource(strings = {"user-1", "user-2", "user-3"})
    void saveProgram_ShouldAcceptDifferentParticipants(String responsibleUserId) {
        System.out.println("➡️ Iniciando prueba para participante: " + responsibleUserId);
            
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
        request.setResponsibleUserId(responsibleUserId);
        request.setObservations("Test observation");

        // Configuración del comportamiento simulado del repositorio.
        // Cuando se busque el último código de programa, devuelve vacío (no hay programas previos).
        when(programRepository.findTopByOrderByProgramCodeDesc()).thenReturn(Mono.empty());

        // Cuando se guarde cualquier entidad, devuelve la misma entidad simulando guardado exitoso.
        when(programRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        // Act & Assert - Ejecutamos el método y validamos la respuesta
        // Llamamos al método del servicio y verificamos la respuesta con StepVerifier (reactor-test).
        StepVerifier.create(distributionProgramService.save(request))
                .expectNextMatches(response -> {
                    System.out.println("✅ Programa creado con responsable: " + response.getResponsibleUserId());
                    return responsibleUserId.equals(response.getResponsibleUserId());
                })
                .verifyComplete();
        System.out.println("✔️ Prueba completada para participante: " + responsibleUserId + "\n");
    }

}
