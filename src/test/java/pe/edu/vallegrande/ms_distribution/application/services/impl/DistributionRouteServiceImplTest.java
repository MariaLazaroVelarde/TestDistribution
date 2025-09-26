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
import pe.edu.vallegrande.ms_distribution.infrastructure.dto.request.DistributionRouteUpdateRequest;
import pe.edu.vallegrande.ms_distribution.infrastructure.exception.CustomException;
import pe.edu.vallegrande.ms_distribution.infrastructure.repository.DistributionRouteRepository;
import reactor.core.publisher.Flux;
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

    /**
     * Escenario Positivo:
     * Debe obtener todas las rutas correctamente.
     */
    @Test
    void getAll_ShouldReturnAllRoutes_WhenCalled() {
        System.out.println("➡️ Iniciando prueba: Obtener todas las rutas");
        
        // Arrange
        List<DistributionRoute> routes = Arrays.asList(
            DistributionRoute.builder()
                .id("route-1")
                .routeCode("RUT001")
                .routeName("Ruta Principal")
                .status(Constants.ACTIVE.name())
                .build(),
            DistributionRoute.builder()
                .id("route-2")
                .routeCode("RUT002")
                .routeName("Ruta Secundaria")
                .status(Constants.INACTIVE.name())
                .build()
        );

        when(routeRepository.findAll()).thenReturn(Flux.fromIterable(routes));

        // Act & Assert
        StepVerifier.create(routeService.getAll())
                .assertNext(route -> {
                    assertEquals("route-1", route.getId());
                    assertEquals("RUT001", route.getRouteCode());
                    assertEquals("Ruta Principal", route.getRouteName());
                    assertEquals(Constants.ACTIVE.name(), route.getStatus());
                    System.out.println("✅ Primera ruta obtenida correctamente");
                })
                .assertNext(route -> {
                    assertEquals("route-2", route.getId());
                    assertEquals("RUT002", route.getRouteCode());
                    assertEquals("Ruta Secundaria", route.getRouteName());
                    assertEquals(Constants.INACTIVE.name(), route.getStatus());
                    System.out.println("✅ Segunda ruta obtenida correctamente");
                })
                .verifyComplete();

        verify(routeRepository).findAll();
        System.out.println("✔️ Prueba de obtener todas las rutas finalizada\n");
    }

    /**
     * Escenario Positivo:
     * Debe obtener todas las rutas activas correctamente.
     */
    @Test
    void getAllActive_ShouldReturnActiveRoutes_WhenCalled() {
        System.out.println("➡️ Iniciando prueba: Obtener rutas activas");
        
        // Arrange
        List<DistributionRoute> activeRoutes = Arrays.asList(
            DistributionRoute.builder()
                .id("route-1")
                .routeCode("RUT001")
                .routeName("Ruta Principal")
                .status(Constants.ACTIVE.name())
                .build(),
            DistributionRoute.builder()
                .id("route-3")
                .routeCode("RUT003")
                .routeName("Ruta Nocturna")
                .status(Constants.ACTIVE.name())
                .build()
        );

        when(routeRepository.findAllByStatus(Constants.ACTIVE.name()))
            .thenReturn(Flux.fromIterable(activeRoutes));

        // Act & Assert
        StepVerifier.create(routeService.getAllActive())
                .assertNext(route -> {
                    assertEquals("route-1", route.getId());
                    assertEquals(Constants.ACTIVE.name(), route.getStatus());
                    System.out.println("✅ Primera ruta activa obtenida correctamente");
                })
                .assertNext(route -> {
                    assertEquals("route-3", route.getId());
                    assertEquals(Constants.ACTIVE.name(), route.getStatus());
                    System.out.println("✅ Segunda ruta activa obtenida correctamente");
                })
                .verifyComplete();

        verify(routeRepository).findAllByStatus(Constants.ACTIVE.name());
        System.out.println("✔️ Prueba de obtener rutas activas finalizada\n");
    }

    /**
     * Escenario Positivo:
     * Debe obtener todas las rutas inactivas correctamente.
     */
    @Test
    void getAllInactive_ShouldReturnInactiveRoutes_WhenCalled() {
        System.out.println("➡️ Iniciando prueba: Obtener rutas inactivas");
        
        // Arrange
        List<DistributionRoute> inactiveRoutes = Arrays.asList(
            DistributionRoute.builder()
                .id("route-2")
                .routeCode("RUT002")
                .routeName("Ruta Secundaria")
                .status(Constants.INACTIVE.name())
                .build()
        );

        when(routeRepository.findAllByStatus(Constants.INACTIVE.name()))
            .thenReturn(Flux.fromIterable(inactiveRoutes));

        // Act & Assert
        StepVerifier.create(routeService.getAllInactive())
                .assertNext(route -> {
                    assertEquals("route-2", route.getId());
                    assertEquals(Constants.INACTIVE.name(), route.getStatus());
                    System.out.println("✅ Ruta inactiva obtenida correctamente");
                })
                .verifyComplete();

        verify(routeRepository).findAllByStatus(Constants.INACTIVE.name());
        System.out.println("✔️ Prueba de obtener rutas inactivas finalizada\n");
    }

    /**
     * Escenario Positivo:
     * Debe obtener una ruta por ID cuando existe.
     */
    @Test
    void getById_ShouldReturnRoute_WhenRouteExists() {
        System.out.println("➡️ Iniciando prueba: Obtener ruta por ID existente");
        
        // Arrange
        String routeId = "route-1";
        DistributionRoute route = DistributionRoute.builder()
                .id(routeId)
                .routeCode("RUT001")
                .routeName("Ruta Principal")
                .status(Constants.ACTIVE.name())
                .build();

        when(routeRepository.findById(routeId)).thenReturn(Mono.just(route));

        // Act & Assert
        StepVerifier.create(routeService.getById(routeId))
                .assertNext(foundRoute -> {
                    assertEquals(routeId, foundRoute.getId());
                    assertEquals("RUT001", foundRoute.getRouteCode());
                    assertEquals("Ruta Principal", foundRoute.getRouteName());
                    assertEquals(Constants.ACTIVE.name(), foundRoute.getStatus());
                    System.out.println("✅ Ruta encontrada correctamente");
                })
                .verifyComplete();

        verify(routeRepository).findById(routeId);
        System.out.println("✔️ Prueba de obtener ruta por ID finalizada\n");
    }

    /**
     * Escenario Negativo:
     * Debe lanzar error cuando se busca una ruta inexistente.
     */
    @Test
    void getById_ShouldReturnError_WhenRouteNotFound() {
        System.out.println("➡️ Iniciando prueba negativa: Obtener ruta inexistente");
        
        // Arrange
        String routeId = "route-inexistente";
        when(routeRepository.findById(routeId)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(routeService.getById(routeId))
                .expectErrorSatisfies(error -> {
                    assertTrue(error instanceof CustomException);
                    CustomException ce = (CustomException) error;
                    assertEquals("Route not found", ce.getErrorMessage().getMessage());
                    assertEquals("Route with ID " + routeId + " does not exist", ce.getErrorMessage().getDetails());
                    System.out.println("❌ Error esperado: " + ce.getMessage());
                })
                .verify();

        verify(routeRepository).findById(routeId);
        System.out.println("✔️ Prueba negativa finalizada con éxito\n");
    }

    /**
     * Escenario Positivo:
     * Debe actualizar una ruta existente correctamente.
     */
    @Test
    void update_ShouldUpdateRoute_WhenRouteExists() {
        System.out.println("➡️ Iniciando prueba: Actualizar ruta existente");
        
        // Arrange
        String routeId = "route-1";
        DistributionRouteUpdateRequest.ZoneEntry zone1 = new DistributionRouteUpdateRequest.ZoneEntry("zone-1", 1, 2);
        DistributionRouteUpdateRequest.ZoneEntry zone2 = new DistributionRouteUpdateRequest.ZoneEntry("zone-2", 2, 3);
        List<DistributionRouteUpdateRequest.ZoneEntry> zones = Arrays.asList(zone1, zone2);

        DistributionRouteUpdateRequest request = new DistributionRouteUpdateRequest();
        request.setRouteName("Ruta Actualizada");
        request.setZones(zones);
        request.setTotalEstimatedDuration(5);
        request.setResponsibleUserId("user-2");

        DistributionRoute existingRoute = DistributionRoute.builder()
                .id(routeId)
                .routeCode("RUT001")
                .routeName("Ruta Original")
                .status(Constants.ACTIVE.name())
                .build();

        DistributionRoute updatedRoute = DistributionRoute.builder()
                .id(routeId)
                .routeCode("RUT001")
                .routeName("Ruta Actualizada")
                .zones(Arrays.asList(
                        new DistributionRoute.ZoneOrder("zone-1", 1, 2),
                        new DistributionRoute.ZoneOrder("zone-2", 2, 3)
                ))
                .totalEstimatedDuration(5)
                .responsibleUserId("user-2")
                .status(Constants.ACTIVE.name())
                .build();

        when(routeRepository.findById(routeId)).thenReturn(Mono.just(existingRoute));
        when(routeRepository.save(any(DistributionRoute.class))).thenReturn(Mono.just(updatedRoute));

        // Act & Assert
        StepVerifier.create(routeService.update(routeId, request))
                .assertNext(route -> {
                    assertEquals(routeId, route.getId());
                    assertEquals("Ruta Actualizada", route.getRouteName());
                    assertEquals(5, route.getTotalEstimatedDuration());
                    assertEquals("user-2", route.getResponsibleUserId());
                    assertEquals(2, route.getZones().size());
                    System.out.println("✅ Ruta actualizada correctamente");
                })
                .verifyComplete();

        verify(routeRepository).findById(routeId);
        verify(routeRepository).save(any(DistributionRoute.class));
        System.out.println("✔️ Prueba de actualización finalizada\n");
    }

    /**
     * Escenario Negativo:
     * Debe lanzar error cuando se intenta actualizar una ruta inexistente.
     */
    @Test
    void update_ShouldReturnError_WhenRouteNotFound() {
        System.out.println("➡️ Iniciando prueba negativa: Actualizar ruta inexistente");
        
        // Arrange
        String routeId = "route-inexistente";
        DistributionRouteUpdateRequest request = new DistributionRouteUpdateRequest();
        request.setRouteName("Ruta Actualizada");
        request.setZones(Arrays.asList(new DistributionRouteUpdateRequest.ZoneEntry("zone-1", 1, 2)));
        request.setTotalEstimatedDuration(3);
        request.setResponsibleUserId("user-1");

        when(routeRepository.findById(routeId)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(routeService.update(routeId, request))
                .verifyComplete();

        verify(routeRepository).findById(routeId);
        verify(routeRepository, never()).save(any(DistributionRoute.class));
        System.out.println("✔️ Prueba negativa de actualización finalizada\n");
    }

    /**
     * Escenario Positivo:
     * Debe eliminar una ruta existente correctamente.
     */
    @Test
    void delete_ShouldDeleteRoute_WhenRouteExists() {
        System.out.println("➡️ Iniciando prueba: Eliminar ruta existente");
        
        // Arrange
        String routeId = "route-1";
        DistributionRoute route = DistributionRoute.builder()
                .id(routeId)
                .routeCode("RUT001")
                .routeName("Ruta a Eliminar")
                .status(Constants.ACTIVE.name())
                .build();

        when(routeRepository.findById(routeId)).thenReturn(Mono.just(route));
        when(routeRepository.delete(any(DistributionRoute.class))).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(routeService.delete(routeId))
                .verifyComplete();

        verify(routeRepository).findById(routeId);
        verify(routeRepository).delete(any(DistributionRoute.class));
        System.out.println("✅ Ruta eliminada correctamente");
        System.out.println("✔️ Prueba de eliminación finalizada\n");
    }

    /**
     * Escenario Negativo:
     * Debe lanzar error cuando se intenta eliminar una ruta inexistente.
     */
    @Test
    void delete_ShouldReturnError_WhenRouteNotFound() {
        System.out.println("➡️ Iniciando prueba negativa: Eliminar ruta inexistente");
        
        // Arrange
        String routeId = "route-inexistente";
        when(routeRepository.findById(routeId)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(routeService.delete(routeId))
                .expectErrorSatisfies(error -> {
                    assertTrue(error instanceof CustomException);
                    CustomException ce = (CustomException) error;
                    assertEquals("Route not found", ce.getErrorMessage().getMessage());
                    assertEquals("Cannot delete non-existent route with id " + routeId, ce.getErrorMessage().getDetails());
                    System.out.println("❌ Error esperado: " + ce.getMessage());
                })
                .verify();

        verify(routeRepository).findById(routeId);
        verify(routeRepository, never()).delete(any(DistributionRoute.class));
        System.out.println("✔️ Prueba negativa de eliminación finalizada\n");
    }

    /**
     * Escenario Negativo:
     * Debe lanzar error cuando se intenta desactivar una ruta inexistente.
     */
    @Test
    void deactivate_ShouldReturnError_WhenRouteNotFound() {
        System.out.println("➡️ Iniciando prueba negativa: Desactivar ruta inexistente");

        // Arrange
        String routeId = "route-inexistente";
        when(routeRepository.findById(routeId)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(routeService.deactivate(routeId))
                .expectErrorSatisfies(error -> {
                    assertTrue(error instanceof CustomException);
                    CustomException ce = (CustomException) error;
                    assertEquals("Route not found", ce.getErrorMessage().getMessage());
                    assertEquals("Cannot change status of non-existent route with id " + routeId, ce.getErrorMessage().getDetails());
                    System.out.println("❌ Error esperado: " + ce.getMessage());
                })
                .verify();

        System.out.println("✔️ Prueba negativa de desactivación finalizada\n");
    }

    /**
     * Escenario de Validación:
     * Debe lanzar NumberFormatException cuando hay un error en el parsing del código.
     */
    @Test
    void generateNextRouteCode_ShouldThrowNumberFormatException_WhenInvalidCodeFormat() {
        System.out.println("➡️ Iniciando prueba: Error de parsing de código inválido");
        
        // Arrange - Simula un código con formato inválido
        DistributionRoute existingRoute = DistributionRoute.builder()
                .routeCode("RUTINVALID")
                .build();

        DistributionRouteCreateRequest request = new DistributionRouteCreateRequest();
        request.setOrganizationId("org-1");
        request.setRouteName("Ruta de Prueba");
        request.setZones(Arrays.asList(new DistributionRouteCreateRequest.ZoneEntry("zone-1", 1, 2)));
        request.setTotalEstimatedDuration(2);
        request.setResponsibleUserId("user-1");

        when(routeRepository.findTopByOrderByRouteCodeDesc()).thenReturn(Mono.just(existingRoute));

        // Act & Assert
        StepVerifier.create(routeService.save(request))
                .expectErrorSatisfies(error -> {
                    assertTrue(error instanceof NumberFormatException);
                    assertEquals("For input string: \"INVALID\"", error.getMessage());
                    System.out.println("❌ Error esperado: " + error.getMessage());
                })
                .verify();

        System.out.println("✔️ Prueba de error de parsing finalizada\n");
    }
}

