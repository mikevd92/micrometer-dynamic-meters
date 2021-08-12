# micrometer-dynamic-meters

Recently I've been working around with micrometer meters, and noticed that they are limited in the sense that unlike the Prometheus java client library you can't create dynamic metrics.

In this article I'm presenting novel way of implementing such dynamic metrics.

For starters we are going to need the following spring boot configurations:
```
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
```
```
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
```
```
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
```
Then we are going to implement a base class which is going to implement the generation of

the key for all our dynamic meters:
```
public class DynamicMeter {
    protected String generateKey(String ...tags) {
        return String.join(":", tags);
    }
}
```
After that we will create the following classes extending the DynamicMeter class:
```
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
```
```
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
```    
```
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
```    
```
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
```
Example usage for DynamicCounter:
```
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
```
Example usage for dynamic gauge:
```
@Component
public class HttpMetricFilter extends HttpFilter {

    public static final String RESPONSE_TIMER = "response_time";
    public static final String PATH = "path";
    public static final String METHOD = "method";
    private final MeterRegistry meterRegistry;
    private final DynamicGauge<AtomicDouble> dynamicGauge;

    public HttpMetricFilter(MeterRegistry meterRegistry){
        Timer.Builder timerBuilder= Timer.builder(RESPONSE_TIMER)
              .description("http request duation")
              .publishPercentileHistogram()
              .publishPercentiles();
        this.meterRegistry=meterRegistry;
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
        AtomicDouble observedObject = new AtomicDouble(0);
        Gauge.Builder builder = Gauge.builder(RESPONSE_TIMER,() -> observedObject)
                    .description("http request duration")
                    .baseUnit("seconds");
        AtomicDouble managedObject=dynamicGauge
              .createOrGet(builder,observedObject,tags);
        managedObject.set(stopWatch.elapsed(TimeUnit.NANOSECONDS)/1_000_000_000.0D);
    }
}
```    
Example usage of DynamicTimer:
```
@Component
public class HttpMetricFilter extends HttpFilter {

    public static final String RESPONSE_TIMER = "response_time";
    public static final String PATH = "path";
    public static final String METHOD = "method";
    private final MeterRegistry meterRegistry;
    private final DynamicTimer dynamicTimer;

    public HttpMetricFilter(MeterRegistry meterRegistry){
        Timer.Builder timerBuilder= Timer.builder(RESPONSE_TIMER)
              .description("http request duation")
              .publishPercentileHistogram()
              .publishPercentiles();
        this.meterRegistry=meterRegistry;
        dynamicTimer =new DynamicTimer(timerBuilder,meterRegistry);
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
        dynamicTimer.tags(tags).record(stopWatch.elapsed(
             TimeUnit.NANOSECONDS),TimeUnit.NANOSECONDS);
    }
}
```
