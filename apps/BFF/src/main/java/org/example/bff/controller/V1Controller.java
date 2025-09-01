package org.example.bff.controller;

import lombok.AllArgsConstructor;
import org.example.bff.domain.request.RequestModel;
import org.example.bff.service.BFFService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("v1")
@AllArgsConstructor
public class V1Controller {
    private BFFService service;

    @PostMapping
    public ResponseEntity<?> speechSearch(@RequestBody final RequestModel request,
                                          @RequestHeader(value = "Accept-Language", defaultValue = "en-gb") final String language) {
        return service.processRequest(request, language);
    }

    @GetMapping("health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("OK");
    }
}

