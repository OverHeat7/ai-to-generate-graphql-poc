package org.example.bff.domain.request;

import lombok.Data;
import lombok.ToString;
import org.example.bff.domain.llm.LLMModel;

import java.util.Map;

@Data
@ToString
public class RequestModel {
    private String textPrompt;
    private Map<String, Object> context;


    // Values used to configure llm (for easier testing)
    private LLMModel llmModel = LLMModel.LLAMA_3_3_70B;
    private boolean shouldCallRealLLM = false;
}

