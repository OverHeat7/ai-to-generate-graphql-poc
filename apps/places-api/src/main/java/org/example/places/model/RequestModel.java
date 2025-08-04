package org.example.places.model;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class RequestModel {
    @NotNull
    @Min(value = -90, message = "Latitude must be between -90 and 90")
    @Max(value = 90, message = "Latitude must be between -90 and 90")
    private Double latitude;

    @NotNull
    @Min(value = -180, message = "Longitude must be between -180 and 180")
    @Max(value = 180, message = "Longitude must be between -180 and 180")
    private Double longitude;

    @NotNull
    @Min(value = 100, message = "Max search distance must be at least 100")
    @Max(value = 100000, message = "Max search distance must not exceed 100000 meters")
    private Double maxSearchDistance = 10000.0; // Default to 10 km if not specified

    @NotNull
    @Min(value = 1, message = "The minimum number of results must be at least 1")
    @Max(value = 1000, message = "The maximum number of results must not exceed 1000")
    @Positive(message = "The maxResults should be a positive number")
    private Integer maxResults = 100; // Default to 100 results if not specified

    @Nullable
    @Size(max = 100, message = "Search query must not exceed 100 characters")
    private String searchQuery;

    @Nullable
    @Size(max = 10, message = "Maximum number of services to filter by is 10")
    private List<POIServices> services; // List of services to filter by, if any

    @Nullable
    private Boolean isOpenNow;
}
