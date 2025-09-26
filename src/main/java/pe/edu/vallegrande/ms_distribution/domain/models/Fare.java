package pe.edu.vallegrande.ms_distribution.domain.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder 
@Document(collection = "fare")
public class Fare {

    @Id
    private String id;

    private String organizationId;

    private String fareCode;
    private String fareName;
    private String fareType;

    private BigDecimal fareAmount;
    private String status;
    private Instant createdAt;

    public void setPrice(Double price) {
        throw new UnsupportedOperationException("Unimplemented method 'setPrice'");
    }
    
    public void setDescription(String description) {
        throw new UnsupportedOperationException("Unimplemented method 'setDescription'");
    }
}
