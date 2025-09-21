package pe.edu.vallegrande.ms_distribution.application.services.impl;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.junit.jupiter.api.BeforeEach;
import pe.edu.vallegrande.ms_distribution.infrastructure.dto.request.FareCreateRequest;
import pe.edu.vallegrande.ms_distribution.infrastructure.repository.FareRepository;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
     * Prueba parametrizada que ejecuta el mismo método varias veces
     * cambiando solo el organizationId. Esto permite validar el comportamiento
     * con diferentes entradas sin duplicar código.
     */
    @ParameterizedTest(name = "Crear tarifa para organización: {0}")
    @ValueSource(strings = {"org-001", "org-002", "org-003"})
    void saveFare_ShouldAcceptDifferentParticipants(String organizationId) {
        System.out.println("➡️ Iniciando prueba para organizationId: " + organizationId);

        // --- Arrange: Configuración de la solicitud que será enviada al servicio ---
        FareCreateRequest request = FareCreateRequest.builder()
                .organizationId(organizationId)
                .fareName("Tarifa Test")
                .fareType("DIARIA")
                .fareAmount(new BigDecimal("20.00"))
                .build();

        // Configuramos los mocks para simular el comportamiento del repositorio:
        // 1. Cuando se busque el último código de tarifa, devolver vacío (no hay registros previos).
        when(fareRepository.findTopByOrderByFareCodeDesc()).thenReturn(Mono.empty());

        // 2. Cuando se verifique si un código existe, devolver false (no existe).
        when(fareRepository.existsByFareCode(anyString())).thenReturn(Mono.just(false));

        // 3. Cuando se guarde una tarifa, devolver el mismo objeto que se envió.
        when(fareRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        // --- Act & Assert: Ejecutamos el método y validamos el resultado ---
        StepVerifier.create(fareService.saveF(request))
                .expectNextMatches(response -> {
                    System.out.println("✅ Tarifa creada con organizationId: " + response.getOrganizationId() +
                            ", fareName: " + response.getFareName() +
                            ", fareAmount: " + response.getFareAmount());
                    return organizationId.equals(response.getOrganizationId());
                })
                .verifyComplete();

        System.out.println("✔️ Prueba completada para organizationId: " + organizationId + "\n");
    }
}
