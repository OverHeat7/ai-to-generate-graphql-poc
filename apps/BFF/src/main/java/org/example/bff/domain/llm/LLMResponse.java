package org.example.bff.domain.llm;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Builder
@NoArgsConstructor
public class LLMResponse {
    private LLMResponseStatus status;
    private String message;

    public LLMResponse(LLMResponseStatus status, String message) {
        this.status = status;
        this.message = message;
        switch (status) {
            case SUCCESS -> log.info("LLM Response: {}", message);
            case ERROR -> log.error("LLM Response Error: {}", message);
            case INFO -> log.info("LLM Response Info: {}", message);
            default -> log.warn("Unexpected LLM Response Status: {}", status);
        }
    }

    public LLMResponse refineQuery() {
        if (status == LLMResponseStatus.SUCCESS) {
            this.message = message
                    .replace("\n", " ")
                    .replaceAll(" {2,}", " ");
        }
        this.message = this.message.trim();
        return this;
    }
}
