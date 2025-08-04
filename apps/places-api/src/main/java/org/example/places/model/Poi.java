package org.example.places.model;

import jakarta.persistence.CollectionTable;
import org.example.places.model.dto.GeoPositionDTO;
import org.example.places.model.dto.PoiDTO;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.locationtech.jts.geom.Point;

import java.util.ArrayList;
import java.util.List;

@Entity(name = "pois")
@Getter
@Setter
@EqualsAndHashCode(exclude = "id")
@ToString
public class Poi {

    @Id
    @Column(name = "id")
    private long id;

    @Column(name = "geom", columnDefinition = "geometry(Point,4326)")
    private Point geom;

    @Column(name = "name")
    private String name;

    @Column(name = "country")
    private String country;

    @Column(name = "state")
    private String state;

    @Column(name = "formattedAddress")
    private String formattedAddress;

    @Column(name = "open24h")
    private boolean open24h;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "tblServices",
            joinColumns = @JoinColumn(name = "poi_id")   // matches tblServices.poi_id
    )
    @Column(name = "service", nullable = false)      // the enum‐value column
    @Enumerated(EnumType.STRING)
    private List<POIServices> services;

    public PoiDTO toDto() {
        return PoiDTO.builder()
                .id(String.valueOf(id))
                .position(GeoPositionDTO.builder().longitude(geom.getX()).latitude(geom.getY()).build())
                .name(name)
                .country(country)
                .state(state)
                .address(formattedAddress)
                .open24h(open24h)
                .services(new ArrayList<>(services))
                .build();
    }
}

