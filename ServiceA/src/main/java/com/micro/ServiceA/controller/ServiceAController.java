package com.micro.ServiceA.controller;


import com.micro.ServiceA.service.SlowService;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/a")
public class ServiceAController {

    public static final Logger logger = LoggerFactory.getLogger(ServiceAController.class);

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private SlowService slowService;

    private static final String BASE_URL = "http://localhost:8081/";
    private int count = 1;
    private static final String SERVICE_A = "serviceA";

    @GetMapping
    @CircuitBreaker(name = SERVICE_A, fallbackMethod = "serviceAFallback")
    @Retry(name = SERVICE_A, fallbackMethod = "serviceAFallback")
    @RateLimiter(name = SERVICE_A, fallbackMethod = "serviceAFallback")
    @Bulkhead(name = SERVICE_A, fallbackMethod = "serviceAFallback")
    public ResponseEntity<String> serviceA() {
        String url = BASE_URL + "b";
        System.out.println("Retry method called " + count++ + " times at " + new Date());
        String res = restTemplate.getForObject(url, String.class);
        return new ResponseEntity<String>(res, HttpStatus.OK);
    }

    public ResponseEntity<String> serviceAFallback(Exception e) {
        String exp = "This is a fallback method for service A. Doesn't permit further calls";
        return new ResponseEntity<String>(exp, HttpStatus.TOO_MANY_REQUESTS);
    }

    @GetMapping("/timeLimiter")
    @TimeLimiter(name = SERVICE_A, fallbackMethod = "timeLimiterServiceAFallBack")
    public CompletableFuture<String> timeLimiterServiceA() {
        return CompletableFuture.supplyAsync(slowService::slowMethod);
    }

    public ResponseEntity<String> timeLimiterServiceAFallBack(RequestNotPermitted e) throws InterruptedException {
        String exp = "timeLimiterServiceAFallBack Doesn't permit further calls";
        return new ResponseEntity<String>(exp, HttpStatus.TOO_MANY_REQUESTS);
    }
}
