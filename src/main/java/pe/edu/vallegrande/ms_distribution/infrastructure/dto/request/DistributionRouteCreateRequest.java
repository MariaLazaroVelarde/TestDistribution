package pe.edu.vallegrande.ms_distribution.infrastructure.dto.request;

import lombok.*;

import java.util.List;
import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistributionRouteCreateRequest {

    @NotBlank(message = "organizationId is required")
    private String organizationId;

    @NotBlank(message = "routeCode is required")
    private String routeCode;

    @NotBlank(message = "routeName is required")
    private String routeName;

    @NotEmpty(message = "zones cannot be empty")
    private List<ZoneEntry> zones;

    @Positive(message = "totalEstimatedDuration must be positive")
    private Integer totalEstimatedDuration; // en horas

    @NotBlank(message = "responsibleUserId is required")
    private String responsibleUserId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ZoneEntry {
        @NotBlank(message = "zoneId is required")
        private String zoneId;

        @NotNull(message = "order is required")
        @Positive(message = "order must be positive")
        private Integer order;

        @NotNull(message = "estimatedDuration is required")
        @Positive(message = "estimatedDuration must be positive")
        private Integer estimatedDuration; // en horas
    }
}
