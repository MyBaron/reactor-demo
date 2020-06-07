package com.example.reactive.demo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

/**
 * @author: baron
 * @date: 2020-06-07 15:40
 **/
@DisplayName("fluxDemo")
public class FluxDemo {

    @Test
    @DisplayName("创建空的flux")
    public void testFlux_empty() {
        Flux<Object> empty = Flux.empty()
                .log();
        StepVerifier.create(empty)
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("创建错误流flux")
    public void testFlux_error() {
        Flux<Object> empty = Flux.error(new Throwable())
                .log();
        StepVerifier.create(empty)
                .expectError()
                .verify();
    }


    @Test
    @DisplayName("创建多个元素的Flux")
    public void testFlux_array() {
        Flux<Integer> fluxArray = Flux.just(1,2,3,4,5,6)
                .log();
        StepVerifier.create(fluxArray)
                .expectNext(1,2,3,4,5,6)
                .verifyComplete();

    }
}
