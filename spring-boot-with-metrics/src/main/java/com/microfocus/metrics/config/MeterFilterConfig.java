package com.microfocus.metrics.config;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.config.MeterFilterReply;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MeterFilterConfig {
    @Value("${metrics.namespace}.")
    private String NAMESPACE;
    @Bean
    public MeterFilter meterFilter() {
        return new MeterFilter() {
            @Override
            public MeterFilterReply accept(Meter.Id id) {
                if(id.getName().startsWith(NAMESPACE+"tomcat.")) {
                    return MeterFilterReply.DENY;
                }
                if(id.getName().startsWith(NAMESPACE+"jvm.")) {
                    return MeterFilterReply.DENY;
                }
                if(id.getName().startsWith(NAMESPACE+"process.")) {
                    return MeterFilterReply.DENY;
                }
                if(id.getName().startsWith(NAMESPACE+"system.")) {
                    return MeterFilterReply.DENY;
                }
                return MeterFilterReply.NEUTRAL;
            }
            @Override
            public Meter.Id map(Meter.Id id) {
                return id.withName(NAMESPACE + id.getName());
            }
        };
    }

}
