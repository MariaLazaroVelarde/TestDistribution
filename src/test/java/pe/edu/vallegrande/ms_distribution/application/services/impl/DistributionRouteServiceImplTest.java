package pe.edu.vallegrande.ms_distribution.application.services.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pe.edu.vallegrande.ms_distribution.domain.enums.Constants;
import pe.edu.vallegrande.ms_distribution.domain.models.DistributionRoute;
import pe.edu.vallegrande.ms_distribution.infrastructure.dto.request.DistributionRouteCreateRequest;
import pe.edu.vallegrande.ms_distribution.infrastructure.dto.response.DistributionRouteResponse;
import pe.edu.vallegrande.ms_distribution.infrastructure.exception.CustomException;
import pe.edu.vallegrande.ms_distribution.infrastructure.repository.DistributionRouteRepository;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class DistributionRouteServiceImplTest {

    @Mock
    private DistributionRouteRepository routeRepository;

    @InjectMocks
    private DistributionRouteServiceImpl routeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Escenario Positivo:
     * Debe crear una ruta válida cuando la solicitud tiene datos correctos.
     */
    @Test
    void save_ShouldCreateRoute_WhenRequestIsValid() {
        System.out.println("➡️ Iniciando prueba: Creando ruta válida");
        
        // Arrange - Construimos la solicitud
        DistributionRouteCreateRequest.ZoneEntry zone1 = new DistributionRouteCreateRequest.ZoneEntry("zone-1", 1, 2);
        DistributionRouteCreateRequest.ZoneEntry zone2 = new DistributionRouteCreateRequest.ZoneEntry("zone-2", 2, 3);
        List<DistributionRouteCreateRequest.ZoneEntry> zones = Arrays.asList(zone1, zone2);

        DistributionRouteCreateRequest request = new DistributionRouteCreateRequest();
        request.setOrganizationId("org-1");
        request.setRouteName("Ruta Principal");
        request.setZones(zones);
        request.setTotalEstimatedDuration(5);
        request.setResponsibleUserId("user-1");

        // Simula que no hay rutas previas (genera RUT001)
        when(routeRepository.findTopByOrderByRouteCodeDesc()).thenReturn(Mono.empty());

        // Capturador para verificar lo que se guarda en el repositorio
        ArgumentCaptor<DistributionRoute> routeCaptor = ArgumentCaptor.forClass(DistributionRoute.class);

        // Simulación del objeto guardado
        DistributionRoute savedRoute = DistributionRoute.builder()
                .id("route-1")
                .organizationId("org-1")
                .routeCode("RUT001")
                .routeName("Ruta Principal")
                .zones(Arrays.asList(
                        DistributionRoute.ZoneOrder.builder()
                                .zoneId("zone-1")
                                .order(1)
                                .estimatedDuration(2)
                                .build(),
                        DistributionRoute.ZoneOrder.builder()
                                .zoneId("zone-2")
                                .order(2)
                                .estimatedDuration(3)
                                .build()
                ))
                .totalEstimatedDuration(5)
                .responsibleUserId("user-1")
                .status(Constants.ACTIVE.name())
                .createdAt(Instant.now())
                .build();

        when(routeRepository.save(any(DistributionRoute.class))).thenReturn(Mono.just(savedRoute));

        // Act & Assert - Ejecutamos el método y validamos la respuesta
        StepVerifier.create(routeService.save(request))
                .assertNext(response -> {
                    System.out.println("✅ Ruta creada correctamente con código: " + response.getRouteCode());
                    assertNotNull(response);
                    assertEquals("route-1", response.getId());
                    assertEquals("org-1", response.getOrganizationId());
                    assertEquals("RUT001", response.getRouteCode());
                    assertEquals("Ruta Principal", response.getRouteName());
                    assertEquals(5, response.getTotalEstimatedDuration());
                    assertEquals("user-1", response.getResponsibleUserId());
                    assertEquals(Constants.ACTIVE.name(), response.getStatus());
                    assertNotNull(response.getCreatedAt());
                    
                    // Validar zonas
                    assertEquals(2, response.getZones().size());
                    assertEquals("zone-1", response.getZones().get(0).getZoneId());
                    assertEquals(1, response.getZones().get(0).getOrder());
                    assertEquals(2, response.getZones().get(0).getEstimatedDuration());
                })
                .verifyComplete();

        // Verifica que los métodos del repositorio fueron llamados correctamente
        verify(routeRepository).findTopByOrderByRouteCodeDesc();
        verify(routeRepository).save(routeCaptor.capture());

        // Validamos los valores capturados antes de guardar
        DistributionRoute routeToSave = routeCaptor.getValue();
        System.out.println("📌 Datos enviados al repositorio:");
        System.out.println("   Organización: " + routeToSave.getOrganizationId());
        System.out.println("   Código: " + routeToSave.getRouteCode());
        System.out.println("   Nombre: " + routeToSave.getRouteName());
        System.out.println("   Duración total: " + routeToSave.getTotalEstimatedDuration());
        System.out.println("   Usuario responsable: " + routeToSave.getResponsibleUserId());
        System.out.println("   Estado: " + routeToSave.getStatus());
        System.out.println("   Número de zonas: " + routeToSave.getZones().size());

        assertEquals("org-1", routeToSave.getOrganizationId());
        assertEquals("RUT001", routeToSave.getRouteCode());
        assertEquals("Ruta Principal", routeToSave.getRouteName());
        assertEquals(5, routeToSave.getTotalEstimatedDuration());
        assertEquals("user-1", routeToSave.getResponsibleUserId());
        assertEquals(Constants.ACTIVE.name(), routeToSave.getStatus());
        assertEquals(2, routeToSave.getZones().size());

        System.out.println("✔️ Prueba finalizada con éxito\n");
    }

    /**
     * Escenario Positivo:
     * Debe generar el siguiente código de ruta correctamente.
     */
    @Test
    void save_ShouldGenerateNextRouteCode_WhenPreviousRoutesExist() {
        System.out.println("➡️ Iniciando prueba: Generación de código secuencial");
        
        // Arrange - Simula que ya existe una ruta con código RUT005
        DistributionRoute existingRoute = DistributionRoute.builder()
                .routeCode("RUT005")
                .build();

        DistributionRouteCreateRequest request = new DistributionRouteCreateRequest();
        request.setOrganizationId("org-1");
        request.setRouteName("Nueva Ruta");
        request.setZones(Arrays.asList(new DistributionRouteCreateRequest.ZoneEntry("zone-1", 1, 2)));
        request.setTotalEstimatedDuration(3);
        request.setResponsibleUserId("user-1");

        when(routeRepository.findTopByOrderByRouteCodeDesc()).thenReturn(Mono.just(existingRoute));
        when(routeRepository.save(any(DistributionRoute.class))).thenReturn(Mono.just(
                DistributionRoute.builder()
                        .id("route-2")
                        .routeCode("RUT006")
                        .organizationId("org-1")
                        .routeName("Nueva Ruta")
                        .zones(Arrays.asList(
                                DistributionRoute.ZoneOrder.builder()
                                        .zoneId("zone-1")
                                        .order(1)
                                        .estimatedDuration(3)
                                        .build()
                        ))
                        .totalEstimatedDuration(3)
                        .responsibleUserId("user-1")
                        .status(Constants.ACTIVE.name())
                        .createdAt(Instant.now())
                        .build()
        ));

        // Act & Assert
        StepVerifier.create(routeService.save(request))
                .assertNext(response -> {
                    assertEquals("RUT006", response.getRouteCode());
                    System.out.println("✅ Código generado correctamente: " + response.getRouteCode());
                })
                .verifyComplete();

        System.out.println("✔️ Prueba de generación de código finalizada\n");
    }

    /**
     * Escenario Negativo:
     * Debe lanzar error cuando el repositorio falla al guardar la ruta.
     */
    @Test
    void save_ShouldReturnError_WhenRepositoryFails() {
        System.out.println("➡️ Iniciando prueba negativa: Falla del repositorio al guardar ruta");

        // Arrange
        DistributionRouteCreateRequest request = new DistributionRouteCreateRequest();
        request.setOrganizationId("org-1");
        request.setRouteName("Ruta de Prueba");
        request.setZones(Arrays.asList(new DistributionRouteCreateRequest.ZoneEntry("zone-1", 1, 2)));
        request.setTotalEstimatedDuration(2);
        request.setResponsibleUserId("user-1");

        when(routeRepository.findTopByOrderByRouteCodeDesc()).thenReturn(Mono.empty());
        when(routeRepository.save(any(DistributionRoute.class)))
                .thenReturn(Mono.error(new RuntimeException("Database error")));

        // Act & Assert
        StepVerifier.create(routeService.save(request))
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
     * Debe activar una ruta existente correctamente.
     */
    @Test
    void activate_ShouldActivateRoute_WhenRouteExists() {
        System.out.println("➡️ Iniciando prueba: Activación de ruta");

        // Arrange
        String routeId = "route-1";
        DistributionRoute existingRoute = DistributionRoute.builder()
                .id(routeId)
                .status(Constants.INACTIVE.name())
                .build();

        when(routeRepository.findById(routeId)).thenReturn(Mono.just(existingRoute));
        when(routeRepository.save(any(DistributionRoute.class))).thenReturn(Mono.just(
                DistributionRoute.builder().id(routeId).status(Constants.ACTIVE.name()).build()
        ));

        // Act & Assert
        StepVerifier.create(routeService.activate(routeId))
                .assertNext(route -> {
                    assertEquals(Constants.ACTIVE.name(), route.getStatus());
                    System.out.println("✅ Ruta activada correctamente");
                })
                .verifyComplete();

        System.out.println("✔️ Prueba de activación finalizada\n");
    }

    /**
     * Escenario de Desactivación:
     * Debe desactivar una ruta existente correctamente.
     */
    @Test
    void deactivate_ShouldDeactivateRoute_WhenRouteExists() {
        System.out.println("➡️ Iniciando prueba: Desactivación de ruta");

        // Arrange
        String routeId = "route-1";
        DistributionRoute existingRoute = DistributionRoute.builder()
                .id(routeId)
                .status(Constants.ACTIVE.name())
                .build();

        when(routeRepository.findById(routeId)).thenReturn(Mono.just(existingRoute));
        when(routeRepository.save(any(DistributionRoute.class))).thenReturn(Mono.just(
                DistributionRoute.builder().id(routeId).status(Constants.INACTIVE.name()).build()
        ));

        // Act & Assert
        StepVerifier.create(routeService.deactivate(routeId))
                .assertNext(route -> {
                    assertEquals(Constants.INACTIVE.name(), route.getStatus());
                    System.out.println("✅ Ruta desactivada correctamente");
                })
                .verifyComplete();

        System.out.println("✔️ Prueba de desactivación finalizada\n");
    }

    /**
     * Escenario Negativo:
     * Debe lanzar error cuando se intenta activar una ruta inexistente.
     */
    @Test
    void activate_ShouldReturnError_WhenRouteNotFound() {
        System.out.println("➡️ Iniciando prueba negativa: Activación de ruta inexistente");

        // Arrange
        String routeId = "route-inexistente";
        when(routeRepository.findById(routeId)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(routeService.activate(routeId))
                .expectErrorSatisfies(error -> {
                    assertTrue(error instanceof CustomException);
                    CustomException ce = (CustomException) error;
                    assertEquals("Route not found", ce.getErrorMessage().getMessage());
                    System.out.println("❌ Error esperado: " + ce.getMessage());
                })
                .verify();

        System.out.println("✔️ Prueba negativa finalizada con éxito\n");
    }

    /**
     * mvn -Dtest=DistributionRouteServiceImplTest test
     */
}

