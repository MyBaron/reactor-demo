package com.example.reactive.demo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.function.Function;

/**
 * @author lanruqi
 * @date 2020/6/4
 */
@DisplayName("transform测试")
public class TransformDemo {



    @Test
    @DisplayName("transform")
    public void testTransform() {
        Function<Flux<String>, Flux<String>> filterAndMap =
                f -> f.filter(color -> !color.equals("orange"))
                        .map(String::toUpperCase);

        Flux<String> tansform = Flux.fromIterable(Arrays.asList("blue", "green", "orange", "purple"))
                .transform(filterAndMap)
                .log();

        StepVerifier.create(tansform)
                .expectNextCount(3)
                .verifyComplete();
    }
}
