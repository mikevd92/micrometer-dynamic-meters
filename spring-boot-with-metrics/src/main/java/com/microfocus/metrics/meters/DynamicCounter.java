package com.microfocus.metrics.meters;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.HashMap;
import java.util.Map;

public class DynamicCounter extends DynamicMeter {
    private MeterRegistry registry;
    private Map<String, Counter> counters = new HashMap<>();
    private Counter.Builder builder;

    public DynamicCounter(Counter.Builder builder,MeterRegistry registry) {
        this.builder=builder;
        this.registry = registry;
    }

    public Counter tags(String ...tags){
        String key = generateKey(tags);
        Counter counter = counters.get(key);
        if(counter == null) {
            counter = builder.tags(tags).register(registry);
            counters.put(key, counter);
        }
        return counter;
    }
}
