package org.example.bff.infrastructure;


public interface BackendGraphQL {
    String fetchGraphQLSchema();

    Object queryGraphQLServer(String query);
}
