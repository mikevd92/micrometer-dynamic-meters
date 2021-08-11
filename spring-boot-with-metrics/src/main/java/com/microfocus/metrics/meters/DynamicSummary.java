package com.microfocus.metrics.meters;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.HashMap;
import java.util.Map;

public class DynamicSummary extends DynamicMeter {
    private MeterRegistry registry;
    private Map<String, DistributionSummary> summaries = new HashMap<>();
    private DistributionSummary.Builder builder;

    public DynamicSummary(DistributionSummary.Builder builder,MeterRegistry registry) {
        this.builder=builder;
        this.registry = registry;
    }

    public DistributionSummary tags(String... tags) {
        String key = generateKey(tags);
        DistributionSummary summary = summaries.get(key);
        if(summary == null) {
            summary = builder.tags(tags).register(registry);
            summaries.put(key, summary);
        }
        return summary;
    }
}
