package org.example.places.controller;

import java.util.List;

import org.example.places.model.RequestModel;
import org.example.places.model.dto.PoiDTO;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.example.places.service.POIService;

@RestController
@RequestMapping("v1")
@AllArgsConstructor
public class RESTController {
    private POIService service;

    @PostMapping("search")
    public ResponseEntity<List<PoiDTO>> searchPois(@Valid @RequestBody final RequestModel request) {
        return ResponseEntity.ok(service.search(request));
    }

    @GetMapping("health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("OK");
    }
}
