package pe.edu.vallegrande.ms_distribution.infrastructure.dto.request;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistributionScheduleCreateRequest {

    private String organizationId;
    private String scheduleCode;
    private String zoneId;
    private String scheduleName;

    private List<String> daysOfWeek;
    private String startTime;   // Formato HH:mm
    private String endTime;     // Formato HH:mm
    private int durationHours;
}
