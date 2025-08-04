package org.example.places.model.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class GeoPositionDTO {
    private Double latitude;
    private Double longitude;
}
