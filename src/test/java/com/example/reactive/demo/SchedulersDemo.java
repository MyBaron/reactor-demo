package com.example.reactive.demo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.Duration;

/**
 * @author lanruqi
 * @date 2020/6/4
 */
@DisplayName("调度器demo")
public class SchedulersDemo {



    @DisplayName("delayElements")
    @Test
    public void testDelayElements() {
        Flux<Integer> delayElementsFlux = Flux.range(0, 10)
                .delayElements(Duration.ofMillis(10))
                .log();

        StepVerifier.create(delayElementsFlux)
                .expectNextCount(10)
                .verifyComplete();

    }

    @Test
    @DisplayName("测试publishOn的异步调用")
    public void testPublishOn() {
        //会等待结果返回吗？
        Flux<Integer> delayElementsFlux = Flux.range(0, 10)
                .publishOn(Schedulers.elastic())
                .map(k -> k * 2)
                .log()
                .publishOn(Schedulers.elastic())
                .map(k -> {
                    try {
                        Thread.sleep(1000);
                        System.out.println("sleep");
                        return k;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return k;
                })
                .log()
                .map(k -> k + 1)
                .log();

        StepVerifier.create(delayElementsFlux)
                .expectNextCount(10)
                .verifyComplete();

    }
}
