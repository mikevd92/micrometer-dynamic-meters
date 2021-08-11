package com.microfocus.metrics;

import com.microfocus.metrics.meters.DynamicCounter;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class HttpMetricInterceptor implements HandlerInterceptor {

     public static final String METHOD = "method";
     public static final String HTTP_REQUESTS_TOTAL = "http_requests_total";
     public static final String PATH = "path";
     public static final String ERROR_COUNT = "error_count";
     public static final String STATUS= "status";
     private final DynamicCounter requestTotalDynamicCounter;
     private final DynamicCounter errorCountDynamicCounter;
     private final MeterRegistry meterRegistry;

     public HttpMetricInterceptor(MeterRegistry meterRegistry){
          this.meterRegistry = meterRegistry;
          this.requestTotalDynamicCounter = new DynamicCounter(Counter.builder(HTTP_REQUESTS_TOTAL).description(
                "total http requests"),meterRegistry);
          this.errorCountDynamicCounter = new DynamicCounter(Counter.builder(ERROR_COUNT).description("http request error count"),meterRegistry);
     }

     @Override
     public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception e)
 throws Exception {
          String[] requestTotalTags = new String[]{
                PATH,
                request.getRequestURI(),
                METHOD,
                request.getMethod()
          };
          requestTotalDynamicCounter.tags(requestTotalTags).increment();
          if(response.getStatus()>=400){
               String[] errorCountTags = new String[]{
                     PATH,
                     request.getRequestURI(),
                     METHOD,
                     request.getMethod(),
                     STATUS,
                     Integer.toString(response.getStatus())
               };
               errorCountDynamicCounter.tags(errorCountTags).increment();
          }
     }
}