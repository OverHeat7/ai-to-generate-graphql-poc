package org.example.bff.model;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RequestModel {
    private String textPrompt;
    private RequestContext context;
}

