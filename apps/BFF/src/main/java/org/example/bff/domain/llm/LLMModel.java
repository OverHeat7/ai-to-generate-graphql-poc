package org.example.bff.domain.llm;

import lombok.Getter;

@Getter
public enum LLMModel {
    NOVA_PRO("amazon.nova-pro-v1:0", false, true),
    TUNED_NOVA_PRO("arn:aws:bedrock:us-east-1:918440787963:custom-model-deployment/8krk6i2bzts0", true, true),
    GPT_4_1_MINI("gpt-4.1-mini-2025-04-14", false, true),
    TUNED_GPT_4_1_MINI("ft:gpt-4.1-mini-2025-04-14:personal:fine-tuned:C6N7LlZJ", true, true);

    private final String modelId;
    private final boolean isFineTuned;
    private final boolean needsFullInstructions;

    LLMModel(final String modelId, final boolean isFineTuned, final boolean needsFullInstructions) {
        this.modelId = modelId;
        this.isFineTuned = isFineTuned;
        this.needsFullInstructions = needsFullInstructions;
    }
}
