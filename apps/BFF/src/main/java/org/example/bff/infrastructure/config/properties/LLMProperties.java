package org.example.bff.infrastructure.config.properties;

import io.github.sashirestela.openai.SimpleOpenAI;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "llm")
public class LLMProperties {
    private String gptApiKey;
    private String gptOrganizationId;
    private String gptProjectId;

    @Bean
    public SimpleOpenAI simpleOpenAI() {
        return SimpleOpenAI.builder()
                .apiKey(gptApiKey)
                .organizationId(gptOrganizationId)
                .projectId(gptProjectId)
                .build();
    }
}
