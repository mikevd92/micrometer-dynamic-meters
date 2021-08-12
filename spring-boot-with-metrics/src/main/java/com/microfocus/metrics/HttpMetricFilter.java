package com.microfocus.metrics;

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.AtomicDouble;
import com.microfocus.metrics.meters.DynamicGauge;
import com.microfocus.metrics.meters.DynamicTimer;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

@Component
public class HttpMetricFilter extends HttpFilter {

    public static final String RESPONSE_TIMER = "response_time";
    public static final String PATH = "path";
    public static final String METHOD = "method";
    private final MeterRegistry meterRegistry;
    //private final DynamicTimer dynamicTimer;
    private final DynamicGauge<AtomicDouble> dynamicGauge;

    public HttpMetricFilter(MeterRegistry meterRegistry){
        //Timer.Builder timerBuilder= Timer.builder(RESPONSE_TIMER)
        //      .description("http request duation")
        //      .publishPercentileHistogram()
        //      .publishPercentiles();
        this.meterRegistry=meterRegistry;
        //dynamicTimer =new DynamicTimer(timerBuilder,meterRegistry);
        dynamicGauge =new DynamicGauge<>(meterRegistry);
    }

    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws
          IOException, ServletException {
        Stopwatch stopWatch = Stopwatch.createStarted();
        chain.doFilter(request, response);
        stopWatch.stop();
        String[] tags = new String[]{
              PATH,
              request.getRequestURI(),
              METHOD,
              request.getMethod()
        };
//        dynamicTimer.tags(tags).record(stopWatch.elapsed(
//             TimeUnit.NANOSECONDS),TimeUnit.NANOSECONDS);
        AtomicDouble observedObject = new AtomicDouble(0);
        Gauge.Builder builder = Gauge.builder(RESPONSE_TIMER,() -> observedObject)
                    .description("http request duration")
                    .baseUnit("seconds");
        AtomicDouble managedObject=dynamicGauge
              .createOrGet(builder,observedObject,tags);
        managedObject.set(stopWatch.elapsed(TimeUnit.NANOSECONDS)/1_000_000_000.0D);
    }
}
