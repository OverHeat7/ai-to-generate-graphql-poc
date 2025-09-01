package org.example.bff.infrastructure.implementations;

import io.github.sashirestela.openai.SimpleOpenAI;
import io.github.sashirestela.openai.domain.chat.ChatMessage;
import io.github.sashirestela.openai.domain.chat.ChatRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.bff.domain.llm.LLMModel;
import org.example.bff.domain.llm.LLMResponse;
import org.example.bff.domain.llm.LLMResponseStatus;
import org.example.bff.metrics.BFFMetrics;
import org.example.bff.infrastructure.LLMRequest;
import org.example.bff.utils.LLMInstructionsGenerator;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.ContentBlock;
import software.amazon.awssdk.services.bedrockruntime.model.ConversationRole;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseRequest;
import software.amazon.awssdk.services.bedrockruntime.model.Message;
import software.amazon.awssdk.services.bedrockruntime.model.SystemContentBlock;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@AllArgsConstructor
public class LLMRequestImpl implements LLMRequest {
    private static final String MOCKED_QUERY_VALUE = "{ searchPOIs( request: { latitude: 30.33218380000011 longitude: -81.655651 maxSearchDistance: 100000 isOpenNow: true services: [PHARMACY] searchQuery: \"2\" maxResults: 5 } ) { id name country services state address open24h position { latitude longitude } } }";
    private BFFMetrics bffMetrics;
    private LLMInstructionsGenerator instructionsGenerator;
    private BedrockRuntimeClient bedrockRuntimeClient;
    private SimpleOpenAI openAi;

    @Override
    public LLMResponse generateGraphQLQuery(final String graphQlSchema, final String textPrompt, final String language,
                                            final Map<String, Object> context, final LLMModel llmModel, boolean shouldCallRealLLM,
                                            boolean returnLLMResponse) {
        // Log the parameters received
        log.info("Generating GraphQL query with textPrompt: {}, language: {}, context: {}", textPrompt, language, context);
        final LLMResponse response;
        if (shouldCallRealLLM) {
            response = fetchQueryFromLLM(graphQlSchema, textPrompt, language, context, llmModel, returnLLMResponse);
        } else {
            response = new LLMResponse(LLMResponseStatus.OK, MOCKED_QUERY_VALUE);
        }
        return response;
    }

    private LLMResponse fetchQueryFromLLM(final String graphQlSchema, final String textPrompt, final String language,
                                          final Map<String, Object> context, final LLMModel llmModel,
                                          boolean returnLLMResponse) {
        final Map<String, Object> llmContext = generateLLMContext(language, context);

        final String prompt = generatePrompt(graphQlSchema, textPrompt, llmModel, llmContext.toString());
        try {
            return switch (llmModel) {
                case NOVA_PRO, TUNED_NOVA_PRO -> getLLMResponseNova(llmModel, prompt, returnLLMResponse);
                case GPT_4_1_MINI, TUNED_GPT_4_1_MINI -> getGPTResponse(llmModel, prompt, returnLLMResponse);
            };
        } catch (SdkClientException e) {
            System.err.printf("ERROR: Can't invoke '%s'. Reason: %s", llmModel.getModelId(), e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private LLMResponse getLLMResponseNova(final LLMModel llmModel, final String prompt, boolean returnLLMResponse) {
        final Message message = Message.builder()
                .content(ContentBlock.fromText(prompt))
                .role(ConversationRole.USER)
                .build();
        final ConverseRequest.Builder request = ConverseRequest.builder()
                .modelId(llmModel.getModelId())
                .messages(message)
                .inferenceConfig(config -> config
                        .maxTokens(500)
                        .temperature(0F)
                        .topP(0F)
                );
        if (llmModel.isFineTuned()) {
            request.system(SystemContentBlock.builder()
                    .text("Generate a <OK/INFO/ERROR>:<message/query> according to the user instruction")
                    .build());
        }
        final String response = bedrockRuntimeClient.converse(request.build()).output().message().content().getFirst().text();
        if (returnLLMResponse) {
            return getLlmResponseRaw(response);
        }
        return createLLMResponseFromActualResponse(response).refineQuery();
    }

    private static LLMResponse getLlmResponseRaw(String response) {
        final LLMResponse llmResponse = new LLMResponse(LLMResponseStatus.OK, response);
        if (response != null && response.startsWith("OK:")) {
            llmResponse.refineQuery();
        }
        return llmResponse;
    }

    private LLMResponse getGPTResponse(final LLMModel llmModel, final String prompt, boolean returnLLMResponse) {
        final ChatRequest chatRequest = ChatRequest.builder()
                .model(llmModel.getModelId())
                .message(ChatMessage.UserMessage.of(prompt))
                .temperature(0.0)
                .maxCompletionTokens(300)
                .build();
        var futureChat = openAi.chatCompletions().create(chatRequest);
        final String response = futureChat.join().firstContent();
        if (returnLLMResponse) {
            return getLlmResponseRaw(response);
        }
        return createLLMResponseFromActualResponse(response).refineQuery();
    }

    private Map<String, Object> generateLLMContext(String language, Map<String, Object> context) {
        final Map<String, Object> llmContext = new HashMap<>();
        if (context != null) {
            llmContext.putAll(context);
        }
        llmContext.putIfAbsent("language", language);

        if (llmContext.containsKey("language")) {
            final String newLanguage = llmContext.get("language").toString().toLowerCase()
                    .replace("_", "-").trim();
            llmContext.put("language", newLanguage);
        }

        return llmContext;
    }

    private LLMResponse createLLMResponseFromActualResponse(final String rawResponse) {
        // extract the first "<chars>:" from the text, if it exists and remove it
        final LLMResponse response;
        if (rawResponse.startsWith("OK:")) {
            response = new LLMResponse(LLMResponseStatus.OK, rawResponse.substring(3).trim());
        } else if (rawResponse.startsWith("ERROR:")) {
            response = new LLMResponse(LLMResponseStatus.ERROR, rawResponse.substring(6).trim());
        } else if (rawResponse.startsWith("INFO:")) {
            response = new LLMResponse(LLMResponseStatus.INFO, rawResponse.substring(5).trim());
        } else {
            bffMetrics.trackLLMResponseStatus(LLMResponseStatus.UNKNOWN);
            throw new RuntimeException("LLM response did not start with 'OK:', 'INFO:' or 'ERROR:'. Response: " + rawResponse);
        }
        bffMetrics.trackLLMResponseStatus(response.getStatus());
        return response;
    }

    private String generatePrompt(final String graphQlSchema, final String textPrompt, final LLMModel llmModel,
                                  final String llmContext) {
        return instructionsGenerator.generateFullInstructionsWithQuery(llmModel, llmContext, textPrompt, graphQlSchema);
    }
}
