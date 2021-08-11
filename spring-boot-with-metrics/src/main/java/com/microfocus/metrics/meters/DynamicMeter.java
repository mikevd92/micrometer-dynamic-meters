package com.microfocus.metrics.meters;

public class DynamicMeter {
    protected String generateKey(String ...tags) {
        return String.join(":", tags);
    }
}
