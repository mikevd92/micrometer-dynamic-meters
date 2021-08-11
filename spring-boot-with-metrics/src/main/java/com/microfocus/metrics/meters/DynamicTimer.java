package com.microfocus.metrics.meters;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.HashMap;
import java.util.Map;

public class DynamicTimer extends DynamicMeter {

    private MeterRegistry registry;
    private Map<String, Timer> timers = new HashMap<>();
    private Timer.Builder builder;

    public DynamicTimer(Timer.Builder builder,MeterRegistry registry) {
        this.builder = builder;
        this.registry = registry;
    }

    public Timer tags(String ...tags){
        String key = generateKey(tags);
        Timer timer = timers.get(key);
        if(timer == null) {
            timer = builder.tags(tags).register(registry);
            timers.put(key, timer);
        }
        return timer;
    }
}