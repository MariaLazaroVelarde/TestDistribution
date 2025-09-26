package pe.edu.vallegrande.ms_distribution.infrastructure.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistributionRouteUpdateRequest {

    @NotBlank(message = "routeName is required")
    private String routeName;

    @NotNull(message = "zones list cannot be null")
    private List<ZoneEntry> zones;

    private Integer totalEstimatedDuration; // en horas
    private String responsibleUserId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ZoneEntry {
        private String zoneId;
        private Integer order;
        private Integer estimatedDuration;
    }
}
