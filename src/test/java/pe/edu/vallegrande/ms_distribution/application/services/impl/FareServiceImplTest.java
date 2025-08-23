package pe.edu.vallegrande.ms_distribution.application.services.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pe.edu.vallegrande.ms_distribution.domain.enums.Constants;
import pe.edu.vallegrande.ms_distribution.domain.models.Fare;
import pe.edu.vallegrande.ms_distribution.infrastructure.dto.request.FareCreateRequest;
import pe.edu.vallegrande.ms_distribution.infrastructure.exception.CustomException;
import pe.edu.vallegrande.ms_distribution.infrastructure.repository.FareRepository;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.math.BigDecimal;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class FareServiceImplTest {

    @Mock
    private FareRepository fareRepository;

    @InjectMocks
    private FareServiceImpl fareService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Escenario Positivo:
     * Debe crear una tarifa válida cuando la solicitud tiene datos correctos.
     */
    @Test
    void saveF_ShouldCreateFare_WhenRequestIsValid() {
        System.out.println("➡️ Iniciando prueba: Creando tarifa válida");
        // Arrange - Construimos la solicitud
        FareCreateRequest request = FareCreateRequest.builder()
                .organizationId("6896b2ecf3e398570ffd99d3")
                .fareName("Tarifa Básica")
                .fareType("SEMANAL")
                .fareAmount(new BigDecimal("15"))
                .build();

        // Simula que no hay tarifas previas (genera TAR001)
        when(fareRepository.findTopByOrderByFareCodeDesc()).thenReturn(Mono.empty());
        // Simula que el código TAR001 no existe
        when(fareRepository.existsByFareCode("TAR001")).thenReturn(Mono.just(false));

        // Capturador para verificar lo que se guarda en el repositorio
        ArgumentCaptor<Fare> fareCaptor = ArgumentCaptor.forClass(Fare.class);

        // Simulación del objeto guardado
        Fare savedFare = Fare.builder()
                .id("")
                .organizationId("6896b2ecf3e398570ffd99d3")
                .fareCode("TAR001")
                .fareName("Tarifa Básica")
                .fareType("SEMANAL")
                .fareAmount(new BigDecimal("15"))
                .status(Constants.ACTIVE.name())
                .createdAt(Instant.now())
                .build();

        when(fareRepository.save(any(Fare.class))).thenReturn(Mono.just(savedFare));

        // Act & Assert - Ejecutamos el método y validamos la respuesta
        StepVerifier.create(fareService.saveF(request))
                .assertNext(response -> {
                    System.out.println("✅ Tarifa creada correctamente con código: " + response.getFareCode());
                    assertNotNull(response);
                    assertEquals("", response.getId());
                    assertEquals("6896b2ecf3e398570ffd99d3", response.getOrganizationId());
                    assertEquals("TAR001", response.getFareCode());
                    assertEquals("Tarifa Básica", response.getFareName());
                    assertEquals("SEMANAL", response.getFareType());
                    assertEquals(new BigDecimal("15"), response.getFareAmount());
                    assertEquals(Constants.ACTIVE.name(), response.getStatus());
                    assertNotNull(response.getCreatedAt());
                })
                .verifyComplete();

        // Verifica que los métodos del repositorio fueron llamados correctamente
        verify(fareRepository).findTopByOrderByFareCodeDesc();
        verify(fareRepository).existsByFareCode("TAR001");
        verify(fareRepository).save(fareCaptor.capture());

        // Validamos los valores capturados antes de guardar
        Fare fareToSave = fareCaptor.getValue();
        System.out.println("📌 Datos enviados al repositorio:");
        System.out.println("   Organización: " + fareToSave.getOrganizationId());
        System.out.println("   Código: " + fareToSave.getFareCode());
        System.out.println("   Nombre: " + fareToSave.getFareName());
        System.out.println("   Tipo: " + fareToSave.getFareType());
        System.out.println("   Monto: " + fareToSave.getFareAmount());
        System.out.println("   Estado: " + fareToSave.getStatus());

        assertEquals("6896b2ecf3e398570ffd99d3", fareToSave.getOrganizationId());
        assertEquals("TAR001", fareToSave.getFareCode());
        assertEquals("Tarifa Básica", fareToSave.getFareName());
        assertEquals("SEMANAL", fareToSave.getFareType());
        assertEquals(new BigDecimal("15"), fareToSave.getFareAmount());
        assertEquals(Constants.ACTIVE.name(), fareToSave.getStatus());
        assertNotNull(fareToSave.getCreatedAt());

        System.out.println("✔️ Prueba finalizada con éxito\n");
    }

    /**
     * Escenario Negativo:
     * No debe crear una tarifa si el código generado ya existe.
     */
    @Test
    void saveF_ShouldReturnError_WhenFareCodeAlreadyExists() {
        System.out.println("➡️ Iniciando prueba negativa: Código de tarifa ya existe");
        // Arrange
        FareCreateRequest request = FareCreateRequest.builder()
                .organizationId("6896b2ecf3e398570ffd99d3")
                .fareName("Tarifa Duplicada")
                .fareType("SEMANAL")
                .fareAmount(new BigDecimal("20"))
                .build();

        // Simula que no hay tarifas previas (genera TAR001)
        when(fareRepository.findTopByOrderByFareCodeDesc()).thenReturn(Mono.empty());
        // Simula que el código TAR001 ya existe
        when(fareRepository.existsByFareCode("TAR001")).thenReturn(Mono.just(true));

        // Act & Assert
        StepVerifier.create(fareService.saveF(request))
                .expectErrorSatisfies(error -> {
                    assertTrue(error instanceof CustomException);
                    CustomException ce = (CustomException) error;
                    assertEquals("Fare code already exists - TAR001", ce.getMessage());
                    System.out.println("❌ Error esperado: " + ce.getMessage());
                })
                .verify();

        // Verifica que no se intentó guardar ninguna tarifa
        verify(fareRepository, never()).save(any(Fare.class));
        System.out.println("✔️ Prueba negativa finalizada con éxito\n");
    }
}