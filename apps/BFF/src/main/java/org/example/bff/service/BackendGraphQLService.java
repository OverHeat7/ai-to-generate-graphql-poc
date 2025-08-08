package org.example.bff.service;


public interface BackendGraphQLService {
    String fetchGraphQLSchema();

    Object queryGraphQLServer(String query);
}
