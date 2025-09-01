package org.example.bff.infrastructure;


public interface BackendGraphQLService {
    String fetchGraphQLSchema();

    Object queryGraphQLServer(String query);
}
