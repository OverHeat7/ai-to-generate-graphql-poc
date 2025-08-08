package org.example.bff.service.implementations;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.bff.domain.llm.LLMModel;
import org.example.bff.service.LLMService;
import org.example.bff.utils.LLMInstructionsGenerator;
import org.json.JSONObject;
import org.json.JSONPointer;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@AllArgsConstructor
public class LLMServiceImpl implements LLMService {
    private static final String QUERY_FORMAT = "{ \"query\": \"%s\" }";
    private static final String MOCKED_QUERY_VALUE = "{ searchPOIs( request: { latitude: 30.33218380000011 longitude: -81.655651 maxSearchDistance: 100000 isOpenNow: true services: [PHARMACY] searchQuery: \\\"2\\\" maxResults: 5 } ) { id name country services state address open24h position { latitude longitude } } }";
    private LLMInstructionsGenerator instructionsGenerator;
    private BedrockRuntimeClient bedrockRuntimeClient;

    @Override
    public String generateGraphQLQuery(final String graphQlSchema, final String textPrompt, final String language,
                                       final Map<String, Object> context, final LLMModel llmModel, boolean shouldCallRealLLM) {
        // Log the parameters received
        log.info("Generating GraphQL query with textPrompt: {}, language: {}, context: {}", textPrompt, language, context);
        final String query;
        if (shouldCallRealLLM) {
            query = fecthQueryFromLLM(graphQlSchema, textPrompt, language, context, llmModel);
        } else {
            query = MOCKED_QUERY_VALUE;
        }
        return QUERY_FORMAT.formatted(query);
    }

    private String fecthQueryFromLLM(final String graphQlSchema, final String textPrompt, final String language,
                                     final Map<String, Object> context, final LLMModel llmModel) {
        // TODO: Implement the actual call to the LLM service.
        final Map<String, Object> llmContext = new HashMap<>();
        if (context != null) {
            llmContext.putAll(context);
        }
        llmContext.putIfAbsent("language", language);

        final String prompt = generatePrompt(graphQlSchema, textPrompt, llmModel, llmContext.toString());
        try {
            // Encode and send the request to the Bedrock Runtime.
            var response = bedrockRuntimeClient.invokeModel(request -> request
                    .body(SdkBytes.fromUtf8String(prompt))
                    .modelId(llmModel.getModelId())
            );

            final String unprocessedQuery = processLLMResponse(response, llmModel);
            return refineQuery(unprocessedQuery);
        } catch (SdkClientException e) {
            System.err.printf("ERROR: Can't invoke '%s'. Reason: %s", llmModel.getModelId(), e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private String refineQuery(final String unprocessedQuery) {
        String query = unprocessedQuery.replace("\n", " ");
        while (query.contains("  ")) {
            query = query.replace("  ", " ");
        }
        return query.trim();
    }

    private String processLLMResponse(final InvokeModelResponse response, final LLMModel llmModel) {
        // Decode the response body.
        var responseBody = new JSONObject(response.body().asUtf8String());

        // Retrieve the generated text from the model's response.
        var text = new JSONPointer(llmModel.getResponsePath()).queryFrom(responseBody).toString();

        // extract the first "<chars>:" from the text, if it exists and remove it
        if (text.startsWith("OK:")) {
            text = text.substring(3).trim();
        } else if (text.startsWith("ERROR:")) {
            text = text.substring(6).trim();
            throw new RuntimeException("LLM returned an error: " + text);
        } else {
            throw new RuntimeException("LLM response did not start with 'OK:' or 'ERROR:'. Response: " + text);
        }
        return text;
    }

    private String generatePrompt(final String graphQlSchema, final String textPrompt, final LLMModel llmModel,
                                  final String llmContext) {
        if (llmModel.isFineTuned()) {
            // TODO: Implement fine-tuned model prompt generation
            throw new UnsupportedOperationException("Fine-tuned model prompt generation is not implemented yet.");
        }
        return llmModel.getPromptTemplate()
                .formatted(instructionsGenerator.generateNotFineTunedInstruction(llmContext, textPrompt, graphQlSchema));
    }


}
