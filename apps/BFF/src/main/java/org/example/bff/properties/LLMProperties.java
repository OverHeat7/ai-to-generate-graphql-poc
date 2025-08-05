package org.example.bff.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "llm")
public class LLMProperties {
    private boolean callRealLLM = true;
    private String llmUrl;
    private String queryFormat = "{ \"query\": \"%s\" }";
    private String mockedQueryValue = "{ searchPOIs( request: { latitude: 30.33218380000011 longitude: -81.655651 maxSearchDistance: 100000 isOpenNow: true services: [PHARMACY] searchQuery: \\\"2\\\" maxResults: 5 } ) { id name country services state address open24h position { latitude longitude } } }";
}
