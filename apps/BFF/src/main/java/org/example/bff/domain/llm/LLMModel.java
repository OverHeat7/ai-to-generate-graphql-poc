package org.example.bff.domain.llm;

import lombok.Getter;

@Getter
public enum LLMModel {
    //    NOVA_MICRO("amazon.nova-micro-v1:0", "%s", null, false, true),
    NOVA_PRO("amazon.nova-pro-v1:0", "%s", null, false, true),
    TUNED_NOVA_PRO("arn:aws:bedrock:us-east-1:918440787963:custom-model-deployment/8krk6i2bzts0", "%s", null, true, true),
    GPT_4_1_MINI("gpt-4.1-mini-2025-04-14", "%s",null,false,true),
    TUNED_GPT_4_1_MINI("ft:gpt-4.1-mini-2025-04-14:personal:fine-tuned:C6N7LlZJ", "%s",null,false,true);

    // Implemented, but since bedrock does not support on-demand fine-tuning for these models, we can't use them,
    // TITAN_TEXT_LITE("amazon.titan-text-lite-v1", "{\"inputText\":\"User: %s\\nBot:\"}", "/results/0/outputText", false, true),

    // can't apply fine-tuning to these models
    // LLAMA_3_3_70B("us.meta.llama3-3-70b-instruct-v1:0", "{ \"prompt\": \"<|begin_of_text|><|start_header_id|>user<|end_header_id|>\\n%s\\n<|eot_id|>\\n<|start_header_id|>assistant<|end_header_id|>\\n\",\"max_gen_len\": 1000,\"temperature\": 0.5 }", "/generation", false, true),
    // MISTRAL_LARGE("mistral.mistral-large-2402-v1:0", "{\"messages\":[{\"role\":\"user\",\"content\":\"%s\"}]}", "/choices/0/message/content", false, true),
    // PIXTRAL_LARGE("us.mistral.pixtral-large-2502-v1:0", "{\"messages\":[{\"role\":\"user\",\"content\":\"%s\"}]}", "/choices/0/message/content", false, true);

    private final String modelId;
    private final String promptTemplate;
    private final String responsePath;
    private final boolean isFineTuned;
    private final boolean needsFullInstructions;

    LLMModel(final String modelId, final String promptTemplate, final String responsePath, final boolean isFineTuned,
             final boolean needsFullInstructions) {
        this.modelId = modelId;
        this.promptTemplate = promptTemplate;
        this.responsePath = responsePath;
        this.isFineTuned = isFineTuned;
        this.needsFullInstructions = needsFullInstructions;
    }
    }
