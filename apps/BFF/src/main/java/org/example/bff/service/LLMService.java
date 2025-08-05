package org.example.bff.service;

public interface LLMService {
    String generateGraphQLQuery(String textPrompt, String language, String context);
}
