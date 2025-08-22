package org.example.bff.service.implementations;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.bff.domain.llm.LLMResponse;
import org.example.bff.domain.request.RequestModel;
import org.example.bff.service.BFFService;
import org.example.bff.service.LLMService;
import org.example.bff.service.BackendGraphQLService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class BFFServiceImpl implements BFFService {
    private LLMService llmService;
    private BackendGraphQLService backendGraphQLService;


    @Override
    public ResponseEntity<?> processRequest(final RequestModel request, final String language) {
        log.info("Processing request: {}", request);
        final String graphQlSchema = backendGraphQLService.fetchGraphQLSchema();
        final LLMResponse llmResponse = llmService.generateGraphQLQuery(graphQlSchema, request.getTextPrompt(), language, request.getContext(), request.getLlmModel(), request.isShouldCallRealLLM(),request.isReturnLLMResponse());
        if (request.isReturnLLMResponse()) {
            return ResponseEntity.ok(llmResponse.getMessage());
        }
        return switch (llmResponse.getStatus()) {
            case SUCCESS -> {
                final Object backendResponse = backendGraphQLService.queryGraphQLServer(llmResponse.getMessage());
                yield backendResponse != null
                        ? ResponseEntity.ok(backendResponse)
                        : ResponseEntity.noContent().build();
            }
            case ERROR -> ResponseEntity.badRequest().body(llmResponse.getMessage());
            case INFO -> ResponseEntity.ok(llmResponse.getMessage());
            default -> ResponseEntity.internalServerError().body("Unexpected response from LLM");
        };
    }
}
