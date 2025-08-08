package org.example.bff.service.implementations;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.bff.domain.request.RequestModel;
import org.example.bff.service.BFFService;
import org.example.bff.service.LLMService;
import org.example.bff.service.BackendGraphQLService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class BFFServiceImpl implements BFFService {
    private LLMService llmService;
    private BackendGraphQLService backendGraphQLService;


    @Override
    public Object processRequest(final RequestModel request, final String language) {
        log.info("Processing request: {}", request);
        final String graphQlSchema = backendGraphQLService.fetchGraphQLSchema();
        final String query = llmService.generateGraphQLQuery(graphQlSchema, request.getTextPrompt(), language, request.getContext(), request.getLlmModel());
        return backendGraphQLService.queryGraphQLServer(query);
    }
}
