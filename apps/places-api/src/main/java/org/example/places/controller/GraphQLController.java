package org.example.places.controller;

import lombok.AllArgsConstructor;
import org.example.places.model.RequestModel;
import org.example.places.model.dto.PoiDTO;
import org.example.places.service.POIService;
import jakarta.validation.Valid;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@AllArgsConstructor
public class GraphQLController {
    private POIService service;

    @QueryMapping
    public List<PoiDTO> searchPOIs(@Valid @Argument final RequestModel request) {
        return service.search(request);
    }
}
