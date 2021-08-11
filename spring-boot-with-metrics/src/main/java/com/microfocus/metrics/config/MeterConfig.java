package com.microfocus.metrics.config;

import com.microfocus.metrics.HttpMetricFilter;
import com.microfocus.metrics.HttpMetricInterceptor;
import io.micrometer.core.aop.CountedAspect;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MeterConfig implements WebMvcConfigurer {

    private final MeterRegistry meterRegistry;

    public MeterConfig(MeterRegistry meterRegistry){
        this.meterRegistry=meterRegistry;
    }

    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    @Bean
    public CountedAspect countedAspect(MeterRegistry registry) { return  new CountedAspect(registry); }

    @Bean
    public FilterRegistrationBean<HttpMetricFilter> myFilterBean(HttpMetricFilter responseTimeFilter) {
        final FilterRegistrationBean<HttpMetricFilter> filterBean = new FilterRegistrationBean<HttpMetricFilter>();
        filterBean.setFilter(responseTimeFilter);
        filterBean.addUrlPatterns("/*");
        return filterBean;
    }

    @Bean
    public HttpMetricInterceptor requestCounterInterceptor(MeterRegistry registry) {
        return new HttpMetricInterceptor(registry);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestCounterInterceptor(meterRegistry));
    }
}