package com.microfocus.metrics.config;

import com.microfocus.metrics.HttpMetricInterceptor;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.CorsEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.servlet.WebMvcEndpointManagementContextConfiguration;
import org.springframework.boot.actuate.endpoint.web.EndpointMediaTypes;
import org.springframework.boot.actuate.endpoint.web.WebEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.annotation.ControllerEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.annotation.ServletEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.servlet.WebMvcEndpointHandlerMapping;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class ActuatorConfig extends WebMvcEndpointManagementContextConfiguration {

    private final HttpMetricInterceptor httpMetricInterceptor;

    public ActuatorConfig(HttpMetricInterceptor httpMetricInterceptor){
        this.httpMetricInterceptor=httpMetricInterceptor;
    }

    public WebMvcEndpointHandlerMapping webEndpointServletHandlerMapping(WebEndpointsSupplier webEndpointsSupplier, ServletEndpointsSupplier servletEndpointsSupplier, ControllerEndpointsSupplier controllerEndpointsSupplier, EndpointMediaTypes endpointMediaTypes, CorsEndpointProperties corsProperties, WebEndpointProperties webEndpointProperties, Environment environment) {
        WebMvcEndpointHandlerMapping mapping = super.webEndpointServletHandlerMapping(webEndpointsSupplier,
              servletEndpointsSupplier,controllerEndpointsSupplier,endpointMediaTypes,corsProperties,
              webEndpointProperties,environment);

        mapping.setInterceptors(httpMetricInterceptor);

        return mapping;
    }
}