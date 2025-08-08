package org.example.bff.domain.llm;

import lombok.Getter;

@Getter
public enum LLMModel {
    LLAMA_3_3_70B("us.meta.llama3-3-70b-instruct-v1:0", "{ \"prompt\": \"<|begin_of_text|><|start_header_id|>user<|end_header_id|>\\n%s\\n<|eot_id|>\\n<|start_header_id|>assistant<|end_header_id|>\\n\",\"max_gen_len\": 1000,\"temperature\": 0.5 }", "/generation", false),
    MISTRAL_LARGE("mistral.mistral-large-2402-v1:0", "{\"messages\":[{\"role\":\"user\",\"content\":\"%s\"}]}", "/choices/0/message/content", false),
    PIXTRAL_LARGE("us.mistral.pixtral-large-2502-v1:0", "{\"messages\":[{\"role\":\"user\",\"content\":\"%s\"}]}", "/choices/0/message/content", false);

    private final String modelId;
    private final String promptTemplate;
    private final String responsePath;
    private final boolean isFineTuned;

    LLMModel(String modelId, String promptTemplate, String responsePath, boolean isFineTuned) {
        this.modelId = modelId;
        this.promptTemplate = promptTemplate;
        this.responsePath = responsePath;
        this.isFineTuned = isFineTuned;
    }
}
