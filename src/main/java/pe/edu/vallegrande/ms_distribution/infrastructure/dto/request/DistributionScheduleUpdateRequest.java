package pe.edu.vallegrande.ms_distribution.infrastructure.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistributionScheduleUpdateRequest {

    @NotBlank(message = "routeId is required")
    private String routeId;

    @NotBlank(message = "dayOfWeek is required")
    private String dayOfWeek;

    @NotNull(message = "startTime is required")
    private String startTime; // formato HH:mm

    @NotNull(message = "endTime is required")
    private String endTime; // formato HH:mm

    @Positive(message = "estimatedDuration must be positive")
    private Integer estimatedDuration; // en minutos
}