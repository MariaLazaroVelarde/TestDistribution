package pe.edu.vallegrande.ms_distribution.infrastructure.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FareUpdateRequest {

    @NotBlank(message = "fareCode is required")
    private String fareCode;

    @NotNull(message = "price is required")
    @Positive(message = "price must be greater than zero")
    private Double price;

    @NotBlank(message = "description is required")
    private String description;

}
