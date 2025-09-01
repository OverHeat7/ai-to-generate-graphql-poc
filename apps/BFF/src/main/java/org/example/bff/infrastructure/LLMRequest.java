package org.example.bff.infrastructure;

import org.example.bff.domain.llm.LLMModel;
import org.example.bff.domain.llm.LLMResponse;

import java.util.Map;

public interface LLMRequest {
    LLMResponse generateGraphQLQuery(String graphQlSchema, String textPrompt, String language, Map<String, Object> requestContext, LLMModel llmModel, boolean shouldCallRealLLM, boolean returnLLMResponse);
}
