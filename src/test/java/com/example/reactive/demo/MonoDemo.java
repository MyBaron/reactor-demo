package com.example.reactive.demo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * @author: baron
 * @date: 2020-06-08 00:08
 **/
@DisplayName("Mono")
public class MonoDemo {

    @Test
    @DisplayName("创建空的Mono")
    public void testMono_empty() {
        Mono<Object> empty = Mono.empty();

        StepVerifier.create(empty)
                .verifyComplete();


    }
}
