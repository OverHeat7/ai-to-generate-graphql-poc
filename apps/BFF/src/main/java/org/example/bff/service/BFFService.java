package org.example.bff.service;

import org.example.bff.domain.request.RequestModel;

public interface BFFService {

    Object processRequest(RequestModel request, String language);
}
