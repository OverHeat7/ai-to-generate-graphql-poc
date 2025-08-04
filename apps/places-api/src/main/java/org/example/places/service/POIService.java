package org.example.places.service;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.places.model.POISpecifications;
import org.example.places.model.Poi;
import org.example.places.model.RequestModel;
import org.example.places.model.dto.PoiDTO;
import org.example.places.repo.POIRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class POIService {

    private POIRepository repo;

    // WGS-84 SRID
    private final GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);

    public List<PoiDTO> search(@Valid final RequestModel request) {
        log.info("Searching POIs with request: {}", request);
        final Point searchLocation = factory.createPoint(new Coordinate(request.getLongitude(), request.getLatitude()));
        Specification<Poi> spec = Specification.unrestricted();
        spec = spec.and(POISpecifications.withinDistance(searchLocation, request.getMaxSearchDistance()))
                .and(POISpecifications.withServices(request.getServices()))
                .and(POISpecifications.withSearchQuery(request.getSearchQuery()))
                .and(POISpecifications.withOpenNow(request.getIsOpenNow()));
        Pageable pageable = PageRequest.of(0, request.getMaxResults()); // First page, maxResults per page
        return repo.findAll(spec,pageable)
                .stream()
                .map(Poi::toDto)
                .toList();
    }
}
