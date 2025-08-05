package org.example.bff.service.implementations;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.bff.model.RequestModel;
import org.example.bff.service.BFFService;
import org.example.bff.service.LLMService;
import org.example.bff.service.PlacesService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class BFFServiceImpl implements BFFService {
    private LLMService llmService;
    private PlacesService placesService;


    @Override
    public Object processRequest(final RequestModel request, final String language) {
        log.info("Processing request: {}", request);
        final String query = llmService.generateGraphQLQuery(request.getTextPrompt(), language, request.getContext().toString());
        return placesService.getPoisFromPlaces(query);
    }
}
