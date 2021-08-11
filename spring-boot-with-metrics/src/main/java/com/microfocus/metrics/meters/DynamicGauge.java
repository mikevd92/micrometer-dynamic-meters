package com.microfocus.metrics.meters;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.HashMap;
import java.util.Map;

public class DynamicGauge<T> extends DynamicMeter {
    private MeterRegistry registry;
    private Map<String, T> observedValues = new HashMap<>();

    public DynamicGauge(MeterRegistry registry) {
        this.registry = registry;
    }

    public T createOrGet(Gauge.Builder builder,T observedObject,String ...tags){
        String key = generateKey(tags);
        T existingObject = observedValues.get(key);
        if(existingObject == null) {
            builder.tags(tags).register(registry);
            existingObject = observedObject;
            observedValues.put(key, existingObject);
        }
        return existingObject;
    }
}
