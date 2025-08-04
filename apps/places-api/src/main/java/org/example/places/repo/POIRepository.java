package org.example.places.repo;


import org.example.places.model.Poi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface POIRepository extends JpaRepository<Poi, Long> {
    Page<Poi> findAll(Specification<Poi> spec, Pageable pageable);
}
