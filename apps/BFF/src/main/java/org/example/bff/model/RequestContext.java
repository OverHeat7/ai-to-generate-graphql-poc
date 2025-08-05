package org.example.bff.model;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RequestContext {
    private Double latitude;
    private Double longitude;
}
