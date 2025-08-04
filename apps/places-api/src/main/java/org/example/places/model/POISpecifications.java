package org.example.places.model;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class POISpecifications {

    public static Specification<Poi> withinDistance(final Point point, final double distance) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(
                criteriaBuilder.function("ST_DistanceSphere", Double.class, root.get("geom"), criteriaBuilder.literal(point)),
                distance);
    }

    public static Specification<Poi> withServices(List<POIServices> svcs) {
        return (root, query, cb) -> {
            if (svcs == null || svcs.isEmpty()) {
                return cb.conjunction();
            }
            // Force DISTINCT because JPA will SELECT PoI JOIN services and might duplicate Poi rows
            query.distinct(true);

            // Join on the "services" collection
            Join<Poi, POIServices> serviceJoin = root.join("services", JoinType.INNER);
            return serviceJoin.in(svcs);
        };
    }

    public static Specification<Poi> withSearchQuery(final String searchQuery) {
        return (root, query, criteriaBuilder) -> {
            if (searchQuery == null || searchQuery.isEmpty()) {
                return criteriaBuilder.conjunction(); // No filtering if no search query specified
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + searchQuery.toLowerCase() + "%");
        };
    }


    public static Specification<Poi> withOpenNow(final Boolean isOpenNow) {
        return (root, query, criteriaBuilder) -> {
            if (isOpenNow == null) {
                return criteriaBuilder.conjunction(); // No filtering if no search query specified
            }
            // If not null, this should match the attribute open24h from the POI, which is also a boolean
            return criteriaBuilder.equal(root.get("open24h"), isOpenNow);
        };
    }
}
