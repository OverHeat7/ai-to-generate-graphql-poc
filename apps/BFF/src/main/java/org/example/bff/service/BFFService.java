package org.example.bff.service;

import org.example.bff.model.RequestModel;

public interface BFFService {

    Object processRequest(RequestModel request, String language);
}
