package org.example.bff.service;

import org.example.bff.domain.request.RequestModel;
import org.springframework.http.ResponseEntity;

public interface BFFService {

    ResponseEntity<?> processRequest(RequestModel request, String language);
}
