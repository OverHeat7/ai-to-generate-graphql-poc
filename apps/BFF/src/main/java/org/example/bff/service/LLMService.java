package org.example.bff.service;

import org.example.bff.domain.llm.LLMModel;

import java.util.Map;

public interface LLMService {
    String generateGraphQLQuery(String graphQlSchema, String textPrompt, String language, Map<String, Object> requestContext, LLMModel llmModel);
}
