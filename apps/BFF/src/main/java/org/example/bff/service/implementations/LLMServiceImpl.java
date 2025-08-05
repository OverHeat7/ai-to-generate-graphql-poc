package org.example.bff.service.implementations;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.bff.properties.LLMProperties;
import org.example.bff.service.LLMService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class LLMServiceImpl implements LLMService {
    private LLMProperties properties;

    @Override
    public String generateGraphQLQuery(String textPrompt, String language, String context) {
        // Log the parameters received
        log.info("Generating GraphQL query with textPrompt: {}, language: {}, context: {}", textPrompt, language, context);
        if (properties.isCallRealLLM()) {
            return callRealLLM(textPrompt, language, context);
        } else {
            return properties.getQueryFormat().formatted(properties.getMockedQueryValue());
        }
    }

    private String callRealLLM(final String textPrompt, final String language, final String context) {
        // TODO: Implement the actual call to the LLM service.
        throw new UnsupportedOperationException("Real LLM call is not implemented yet. Please set 'llm.call-real-llm' to false to use mocked responses.");
    }
}
