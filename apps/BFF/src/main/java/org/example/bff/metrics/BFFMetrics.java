package org.example.bff.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.example.bff.domain.llm.LLMResponseStatus;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class BFFMetrics {
    private final static String STATUS_DIMENSION = "status";

    final Map<LLMResponseStatus, AtomicInteger> llmResponseStatusCount;
    final Map<Boolean, AtomicInteger> queriesOrMutationsValidCount;

    public BFFMetrics(final MeterRegistry registry) {
        llmResponseStatusCount = new HashMap<>();
        for (final LLMResponseStatus value : LLMResponseStatus.values()) {
            llmResponseStatusCount.put(value, new AtomicInteger(0));
            registry.gauge("llm_response_status_count", createTags(Map.of(STATUS_DIMENSION, value.name())), llmResponseStatusCount, map -> map.get(value).get());
        }
        queriesOrMutationsValidCount = new HashMap<>();
        queriesOrMutationsValidCount.put(true, new AtomicInteger(0));
        queriesOrMutationsValidCount.put(false, new AtomicInteger(0));
        registry.gauge("queries_or_mutations_valid_count", createTags(Map.of("is_valid", "true")), queriesOrMutationsValidCount, map -> map.get(true).get());
        registry.gauge("queries_or_mutations_valid_count", createTags(Map.of("is_valid", "false")), queriesOrMutationsValidCount, map -> map.get(false).get());
    }

    public void trackLLMResponseStatus(final LLMResponseStatus status) {
        if (llmResponseStatusCount.containsKey(status)) {
            llmResponseStatusCount.get(status).incrementAndGet();
        }else{
            llmResponseStatusCount.get(LLMResponseStatus.UNKNOWN).incrementAndGet();
        }
    }

    public void trackQueriesOrMutationsValidCount(final boolean isValid) {
        if (queriesOrMutationsValidCount.containsKey(isValid)) {
            queriesOrMutationsValidCount.get(isValid).incrementAndGet();
        }
    }

    private List<Tag> createTags(Map<String, String> tags) {
        return tags.entrySet().stream()
                .map(entry -> Tag.of(entry.getKey(), entry.getValue()))
                .toList();
    }
}
