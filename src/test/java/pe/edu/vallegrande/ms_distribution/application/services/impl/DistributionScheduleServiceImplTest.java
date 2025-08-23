package pe.edu.vallegrande.ms_distribution.application.services.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pe.edu.vallegrande.ms_distribution.domain.enums.Constants;
import pe.edu.vallegrande.ms_distribution.domain.models.DistributionSchedule;
import pe.edu.vallegrande.ms_distribution.infrastructure.dto.request.DistributionScheduleCreateRequest;
import pe.edu.vallegrande.ms_distribution.infrastructure.dto.response.DistributionScheduleResponse;
import pe.edu.vallegrande.ms_distribution.infrastructure.exception.CustomException;
import pe.edu.vallegrande.ms_distribution.infrastructure.repository.DistributionScheduleRepository;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class DistributionScheduleServiceImplTest {

    @Mock
    private DistributionScheduleRepository scheduleRepository;

    @InjectMocks
    private DistributionScheduleServiceImpl scheduleService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Escenario Positivo:
     * Debe crear un horario válido cuando la solicitud tiene datos correctos.
     */
    @Test
    void save_ShouldCreateSchedule_WhenRequestIsValid() {
        System.out.println("➡️ Iniciando prueba: Creando horario válido");
        
        // Arrange - Construimos la solicitud
        List<String> daysOfWeek = Arrays.asList("LUNES", "MIÉRCOLES", "VIERNES");
        
        DistributionScheduleCreateRequest request = new DistributionScheduleCreateRequest();
        request.setOrganizationId("org-1");
        request.setZoneId("zone-1");
        request.setScheduleName("Horario Zona Centro");
        request.setDaysOfWeek(daysOfWeek);
        request.setStartTime("06:00");
        request.setEndTime("12:00");
        request.setDurationHours(6);

        // Simula que no hay horarios previos (genera HOR001)
        when(scheduleRepository.findTopByOrderByScheduleCodeDesc()).thenReturn(Mono.empty());
        when(scheduleRepository.existsByScheduleCode("HOR001")).thenReturn(Mono.just(false));

        // Capturador para verificar lo que se guarda en el repositorio
        ArgumentCaptor<DistributionSchedule> scheduleCaptor = ArgumentCaptor.forClass(DistributionSchedule.class);

        // Simulación del objeto guardado
        DistributionSchedule savedSchedule = DistributionSchedule.builder()
                .id("schedule-1")
                .organizationId("org-1")
                .scheduleCode("HOR001")
                .zoneId("zone-1")
                .scheduleName("Horario Zona Centro")
                .daysOfWeek(daysOfWeek)
                .startTime("06:00")
                .endTime("12:00")
                .durationHours(6)
                .status(Constants.ACTIVE.name())
                .createdAt(Instant.now())
                .build();

        when(scheduleRepository.save(any(DistributionSchedule.class))).thenReturn(Mono.just(savedSchedule));

        // Act & Assert - Ejecutamos el método y validamos la respuesta
        StepVerifier.create(scheduleService.save(request))
                .assertNext(response -> {
                    System.out.println("✅ Horario creado correctamente con código: " + response.getScheduleCode());
                    assertNotNull(response);
                    assertEquals("schedule-1", response.getId());
                    assertEquals("org-1", response.getOrganizationId());
                    assertEquals("HOR001", response.getScheduleCode());
                    assertEquals("zone-1", response.getZoneId());
                    assertEquals("Horario Zona Centro", response.getScheduleName());
                    assertEquals(3, response.getDaysOfWeek().size());
                    assertEquals("LUNES", response.getDaysOfWeek().get(0));
                    assertEquals("MIÉRCOLES", response.getDaysOfWeek().get(1));
                    assertEquals("VIERNES", response.getDaysOfWeek().get(2));
                    assertEquals("06:00", response.getStartTime());
                    assertEquals("12:00", response.getEndTime());
                    assertEquals(6, response.getDurationHours());
                    assertEquals(Constants.ACTIVE.name(), response.getStatus());
                    assertNotNull(response.getCreatedAt());
                })
                .verifyComplete();

        // Verifica que los métodos del repositorio fueron llamados correctamente
        verify(scheduleRepository).findTopByOrderByScheduleCodeDesc();
        verify(scheduleRepository).existsByScheduleCode("HOR001");
        verify(scheduleRepository).save(scheduleCaptor.capture());

        // Validamos los valores capturados antes de guardar
        DistributionSchedule scheduleToSave = scheduleCaptor.getValue();
        System.out.println("📌 Datos enviados al repositorio:");
        System.out.println("   Organización: " + scheduleToSave.getOrganizationId());
        System.out.println("   Código: " + scheduleToSave.getScheduleCode());
        System.out.println("   Zona: " + scheduleToSave.getZoneId());
        System.out.println("   Nombre: " + scheduleToSave.getScheduleName());
        System.out.println("   Días: " + scheduleToSave.getDaysOfWeek());
        System.out.println("   Hora inicio: " + scheduleToSave.getStartTime());
        System.out.println("   Hora fin: " + scheduleToSave.getEndTime());
        System.out.println("   Duración: " + scheduleToSave.getDurationHours());
        System.out.println("   Estado: " + scheduleToSave.getStatus());

        assertEquals("org-1", scheduleToSave.getOrganizationId());
        assertEquals("HOR001", scheduleToSave.getScheduleCode());
        assertEquals("zone-1", scheduleToSave.getZoneId());
        assertEquals("Horario Zona Centro", scheduleToSave.getScheduleName());
        assertEquals(3, scheduleToSave.getDaysOfWeek().size());
        assertEquals("06:00", scheduleToSave.getStartTime());
        assertEquals("12:00", scheduleToSave.getEndTime());
        assertEquals(6, scheduleToSave.getDurationHours());
        assertEquals(Constants.ACTIVE.name(), scheduleToSave.getStatus());

        System.out.println("✔️ Prueba finalizada con éxito\n");
    }

    /**
     * Escenario Positivo:
     * Debe generar el siguiente código de horario correctamente.
     */
    @Test
    void save_ShouldGenerateNextScheduleCode_WhenPreviousSchedulesExist() {
        System.out.println("➡️ Iniciando prueba: Generación de código secuencial");
        
        // Arrange - Simula que ya existe un horario con código HOR007
        DistributionSchedule existingSchedule = DistributionSchedule.builder()
                .scheduleCode("HOR007")
                .build();

        DistributionScheduleCreateRequest request = new DistributionScheduleCreateRequest();
        request.setOrganizationId("org-1");
        request.setZoneId("zone-1");
        request.setScheduleName("Nuevo Horario");
        request.setDaysOfWeek(Arrays.asList("LUNES", "MARTES"));
        request.setStartTime("08:00");
        request.setEndTime("16:00");
        request.setDurationHours(8);

        when(scheduleRepository.findTopByOrderByScheduleCodeDesc()).thenReturn(Mono.just(existingSchedule));
        when(scheduleRepository.existsByScheduleCode("HOR008")).thenReturn(Mono.just(false));
        when(scheduleRepository.save(any(DistributionSchedule.class))).thenReturn(Mono.just(
                DistributionSchedule.builder().id("schedule-2").scheduleCode("HOR008").build()
        ));

        // Act & Assert
        StepVerifier.create(scheduleService.save(request))
                .assertNext(response -> {
                    assertEquals("HOR008", response.getScheduleCode());
                    System.out.println("✅ Código generado correctamente: " + response.getScheduleCode());
                })
                .verifyComplete();

        System.out.println("✔️ Prueba de generación de código finalizada\n");
    }

    /**
     * Escenario Negativo:
     * No debe crear un horario si el código generado ya existe.
     */
    @Test
    void save_ShouldReturnError_WhenScheduleCodeAlreadyExists() {
        System.out.println("➡️ Iniciando prueba negativa: Código de horario ya existe");

        // Arrange
        DistributionScheduleCreateRequest request = new DistributionScheduleCreateRequest();
        request.setOrganizationId("org-1");
        request.setZoneId("zone-1");
        request.setScheduleName("Horario Duplicado");
        request.setDaysOfWeek(Arrays.asList("LUNES"));
        request.setStartTime("09:00");
        request.setEndTime("17:00");
        request.setDurationHours(8);

        // Simula que no hay horarios previos (genera HOR001)
        when(scheduleRepository.findTopByOrderByScheduleCodeDesc()).thenReturn(Mono.empty());
        // Simula que el código HOR001 ya existe
        when(scheduleRepository.existsByScheduleCode("HOR001")).thenReturn(Mono.just(true));

        // Act & Assert
        StepVerifier.create(scheduleService.save(request))
                .expectErrorSatisfies(error -> {
                    assertTrue(error instanceof CustomException);
                    CustomException ce = (CustomException) error;
                                         assertEquals("Schedule code already exists", ce.getMessage());
                    System.out.println("❌ Error esperado: " + ce.getMessage());
                })
                .verify();

        // Verifica que no se intentó guardar ningún horario
        verify(scheduleRepository, never()).save(any(DistributionSchedule.class));
        System.out.println("✔️ Prueba negativa finalizada con éxito\n");
    }

    /**
     * Escenario Negativo:
     * Debe lanzar error cuando el repositorio falla al guardar el horario.
     */
    @Test
    void save_ShouldReturnError_WhenRepositoryFails() {
        System.out.println("➡️ Iniciando prueba negativa: Falla del repositorio al guardar horario");

        // Arrange
        DistributionScheduleCreateRequest request = new DistributionScheduleCreateRequest();
        request.setOrganizationId("org-1");
        request.setZoneId("zone-1");
        request.setScheduleName("Horario de Prueba");
        request.setDaysOfWeek(Arrays.asList("LUNES"));
        request.setStartTime("08:00");
        request.setEndTime("16:00");
        request.setDurationHours(8);

        when(scheduleRepository.findTopByOrderByScheduleCodeDesc()).thenReturn(Mono.empty());
        when(scheduleRepository.existsByScheduleCode("HOR001")).thenReturn(Mono.just(false));
        when(scheduleRepository.save(any(DistributionSchedule.class)))
                .thenReturn(Mono.error(new RuntimeException("Database error")));

        // Act & Assert
        StepVerifier.create(scheduleService.save(request))
                .expectErrorSatisfies(error -> {
                    assertTrue(error instanceof RuntimeException);
                    assertEquals("Database error", error.getMessage());
                    System.out.println("❌ Error esperado: " + error.getMessage());
                })
                .verify();

        System.out.println("✔️ Prueba negativa finalizada con éxito\n");
    }

    /**
     * Escenario de Activación:
     * Debe activar un horario existente correctamente.
     */
    @Test
    void activate_ShouldActivateSchedule_WhenScheduleExists() {
        System.out.println("➡️ Iniciando prueba: Activación de horario");

        // Arrange
        String scheduleId = "schedule-1";
        DistributionSchedule existingSchedule = DistributionSchedule.builder()
                .id(scheduleId)
                .status(Constants.INACTIVE.name())
                .build();

        when(scheduleRepository.findById(scheduleId)).thenReturn(Mono.just(existingSchedule));
        when(scheduleRepository.save(any(DistributionSchedule.class))).thenReturn(Mono.just(
                DistributionSchedule.builder().id(scheduleId).status(Constants.ACTIVE.name()).build()
        ));

        // Act & Assert
        StepVerifier.create(scheduleService.activate(scheduleId))
                .assertNext(schedule -> {
                    assertEquals(Constants.ACTIVE.name(), schedule.getStatus());
                    System.out.println("✅ Horario activado correctamente");
                })
                .verifyComplete();

        System.out.println("✔️ Prueba de activación finalizada\n");
    }

    /**
     * Escenario de Desactivación:
     * Debe desactivar un horario existente correctamente.
     */
    @Test
    void deactivate_ShouldDeactivateSchedule_WhenScheduleExists() {
        System.out.println("➡️ Iniciando prueba: Desactivación de horario");

        // Arrange
        String scheduleId = "schedule-1";
        DistributionSchedule existingSchedule = DistributionSchedule.builder()
                .id(scheduleId)
                .status(Constants.ACTIVE.name())
                .build();

        when(scheduleRepository.findById(scheduleId)).thenReturn(Mono.just(existingSchedule));
        when(scheduleRepository.save(any(DistributionSchedule.class))).thenReturn(Mono.just(
                DistributionSchedule.builder().id(scheduleId).status(Constants.INACTIVE.name()).build()
        ));

        // Act & Assert
        StepVerifier.create(scheduleService.deactivate(scheduleId))
                .assertNext(schedule -> {
                    assertEquals(Constants.INACTIVE.name(), schedule.getStatus());
                    System.out.println("✅ Horario desactivado correctamente");
                })
                .verifyComplete();

        System.out.println("✔️ Prueba de desactivación finalizada\n");
    }

    /**
     * Escenario Negativo:
     * Debe lanzar error cuando se intenta activar un horario inexistente.
     */
    @Test
    void activate_ShouldReturnError_WhenScheduleNotFound() {
        System.out.println("➡️ Iniciando prueba negativa: Activación de horario inexistente");

        // Arrange
        String scheduleId = "schedule-inexistente";
        when(scheduleRepository.findById(scheduleId)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(scheduleService.activate(scheduleId))
                .expectErrorSatisfies(error -> {
                    assertTrue(error instanceof CustomException);
                    CustomException ce = (CustomException) error;
                    assertEquals("Schedule not found", ce.getErrorMessage().getMessage());
                    System.out.println("❌ Error esperado: " + ce.getMessage());
                })
                .verify();

        System.out.println("✔️ Prueba negativa finalizada con éxito\n");
    }

    /**
     * Escenario de Validación:
     * Debe manejar correctamente horarios con diferentes configuraciones de días.
     */
    @Test
    void save_ShouldHandleDifferentDayConfigurations_WhenRequestIsValid() {
        System.out.println("➡️ Iniciando prueba: Diferentes configuraciones de días");
        
        // Arrange - Horario de fin de semana
        List<String> weekendDays = Arrays.asList("SÁBADO", "DOMINGO");
        
        DistributionScheduleCreateRequest request = new DistributionScheduleCreateRequest();
        request.setOrganizationId("org-1");
        request.setZoneId("zone-2");
        request.setScheduleName("Horario Fin de Semana");
        request.setDaysOfWeek(weekendDays);
        request.setStartTime("10:00");
        request.setEndTime("18:00");
        request.setDurationHours(8);

        when(scheduleRepository.findTopByOrderByScheduleCodeDesc()).thenReturn(Mono.empty());
        when(scheduleRepository.existsByScheduleCode("HOR001")).thenReturn(Mono.just(false));
        when(scheduleRepository.save(any(DistributionSchedule.class))).thenReturn(Mono.just(
                DistributionSchedule.builder()
                        .id("schedule-weekend")
                        .scheduleCode("HOR001")
                        .daysOfWeek(weekendDays)
                        .build()
        ));

        // Act & Assert
        StepVerifier.create(scheduleService.save(request))
                .assertNext(response -> {
                    assertEquals(2, response.getDaysOfWeek().size());
                    assertEquals("SÁBADO", response.getDaysOfWeek().get(0));
                    assertEquals("DOMINGO", response.getDaysOfWeek().get(1));
                    System.out.println("✅ Horario de fin de semana creado correctamente");
                })
                .verifyComplete();

        System.out.println("✔️ Prueba de configuración de días finalizada\n");
    }

    /**
     * mvn -Dtest=DistributionScheduleServiceImplTest test
     */
}

