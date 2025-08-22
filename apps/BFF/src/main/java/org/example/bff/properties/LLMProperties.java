package org.example.bff.properties;

import io.github.sashirestela.cleverclient.client.OkHttpClientAdapter;
import io.github.sashirestela.openai.SimpleOpenAI;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.Executors;

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
