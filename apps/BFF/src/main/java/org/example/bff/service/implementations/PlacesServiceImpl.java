package org.example.bff.service.implementations;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.bff.properties.PlacesProperties;
import org.example.bff.service.PlacesService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class PlacesServiceImpl implements PlacesService {
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public PlacesServiceImpl(final PlacesProperties placesProperties) {
        this.restClient = RestClient.builder()
                .baseUrl(placesProperties.getUrl())
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Object getPoisFromPlaces(final String query) {
        return restClient.post()
                .header("Content-Type", "application/json")
                .body(query)
                .retrieve()
                .body(Object.class);
    }
}
