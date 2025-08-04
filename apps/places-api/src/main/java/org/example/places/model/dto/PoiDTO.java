package org.example.places.model.dto;

import lombok.Builder;
import lombok.Data;
import org.example.places.model.POIServices;

import java.util.List;

@Builder
@Data
public class PoiDTO {
    private String id;
    private GeoPositionDTO position;
    private String name;
    private String country;
    private String state;
    private String address;
    private boolean open24h;
    private List<POIServices> services;
}
