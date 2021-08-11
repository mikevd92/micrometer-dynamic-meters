package com.microfocus.demo;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicLong;

@RestController
public class GreetingController {

    private static final String template = "Hello, %s!";
    private final AtomicLong greetingCounter = new AtomicLong();
    private final AtomicLong meetingCounter = new AtomicLong();

    private final MeterRegistry registry;

    /**
     * We inject the MeterRegistry into this class
     */
    public GreetingController(MeterRegistry registry) {
        this.registry = registry;
    }

    /**
     * The @Timed annotation adds timing support, so we can see how long it takes to execute in Prometheus percentiles
     */
    @GetMapping("/greeting")
    //@Timed(value = "greeting.time", description = "Time taken to return greeting",
          //histogram = true)
    public ResponseEntity<Greeting> greeting(@RequestParam(value = "name", defaultValue = "World") String name)
          throws InterruptedException {
        if(greetingCounter.get() % 2 != 0) {
            return ResponseEntity.accepted()
                  .body(new Greeting(greetingCounter.incrementAndGet(), String.format(template,
                        name)));
        }else{
            greetingCounter.incrementAndGet();
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/meeting")
    //@Timed(value = "greeting.time", description = "Time taken to return greeting",
    //histogram = true)
    public ResponseEntity<Greeting> meeting(@RequestParam(value = "name", defaultValue = "World") String name)
          throws InterruptedException {
        Thread.sleep(2000);
        if(meetingCounter.get() % 2 != 0) {
            return ResponseEntity.accepted()
                  .body(new Greeting(meetingCounter.incrementAndGet(), String.format(template,
                        name)));
        }else{
            meetingCounter.incrementAndGet();
            return ResponseEntity.badRequest().body(null);
        }
    }
}
