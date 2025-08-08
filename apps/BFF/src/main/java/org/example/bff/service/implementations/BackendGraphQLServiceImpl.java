package org.example.bff.service.implementations;

import graphql.introspection.IntrospectionResultToSchema;
import graphql.language.AstPrinter;
import graphql.language.Document;
import lombok.extern.slf4j.Slf4j;
import org.example.bff.properties.PlacesProperties;
import org.example.bff.service.BackendGraphQLService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Slf4j
@Service
public class BackendGraphQLServiceImpl implements BackendGraphQLService {
    private static final String INTROSPECTION_QUERY = """
            query IntrospectionQuery {
              __schema {
                queryType { name }
                mutationType { name }
                subscriptionType { name }
                types {
                  ...FullType
                }
                directives {
                  name
                  locations
                  args {
                    ...InputValue
                  }
                }
              }
            }
            fragment FullType on __Type {
              kind
              name
              description
              fields(includeDeprecated: true) {
                name
                description
                args { ...InputValue }
                type { ...TypeRef }
                isDeprecated
                deprecationReason
              }
              inputFields { ...InputValue }
              interfaces { ...TypeRef }
              enumValues(includeDeprecated: true) {
                name
                description
                isDeprecated
                deprecationReason
              }
              possibleTypes { ...TypeRef }
            }
            fragment InputValue on __InputValue {
              name
              description
              type { ...TypeRef }
              defaultValue
            }
            fragment TypeRef on __Type {
              kind
              name
              ofType {
                kind
                name
                ofType {
                  kind
                  name
                  ofType {
                    kind
                    name
                  }
                }
              }
            }
            """;
    private final RestClient restClient;
    private final IntrospectionResultToSchema introspectionResultToSchema;

    public BackendGraphQLServiceImpl(final PlacesProperties placesProperties) {
        this.restClient = RestClient.builder()
                .baseUrl(placesProperties.getUrl())
                .build();
        this.introspectionResultToSchema = new IntrospectionResultToSchema();
    }

    @Override
    @Cacheable(value = "placesSchema", key = "'schema'")
    public String fetchGraphQLSchema() {
        final Map<String, Object> request = Map.of("query", INTROSPECTION_QUERY);
        final Map<?, ?> introspectionResult = restClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(Map.class);
        final Document schema = introspectionResultToSchema.createSchemaDefinition((Map<String, Object>) introspectionResult.get("data"));
        return AstPrinter.printAst(schema)
                .replace("\r\n", "\n") // Remove carriage returns
                .replaceAll(" {2,}", " "); // Replace multiple spaces with a single space
    }

    @Override
    public Object queryGraphQLServer(final String query) {
        return restClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .body(query)
                .retrieve()
                .body(Object.class);
    }
}
