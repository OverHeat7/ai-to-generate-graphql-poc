package org.example.bff.service;

import org.example.bff.domain.llm.LLMModel;
import org.example.bff.domain.llm.LLMResponse;

import java.util.Map;

public interface LLMService {
    LLMResponse generateGraphQLQuery(String graphQlSchema, String textPrompt, String language, Map<String, Object> requestContext, LLMModel llmModel, boolean shouldCallRealLLM, boolean returnLLMResponse);
}
