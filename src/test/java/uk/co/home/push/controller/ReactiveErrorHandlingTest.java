package uk.co.home.push.controller;

import org.junit.jupiter.api.Test;

import reactor.core.publisher.Mono;

public class ReactiveErrorHandlingTest {

    @Test
    public void testErrorReporting() {
        Mono<?> mono = Mono.just("hey")
                		   .map(r -> new RuntimeException());
        
        mono.doOnError(e -> System.out.println("doOnError: " + e))
			.subscribe(System.out::println);
    }
}