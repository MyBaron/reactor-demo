package com.example.reactive.demo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.ParallelFlux;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Random;

/**
 * @author lanruqi
 * @date 2020/6/4
 */
@DisplayName("并行执行")
public class ParallerDemo {


    @Test
    @DisplayName("并行执行demo")
    public void testParallel(){
        ParallelFlux<Integer> flux = Flux.range(1, 100)
                .parallel()
                .runOn(Schedulers.parallel())
                .map(k->{
                    Random random = new Random();
                    int i = random.nextInt(100);
                    if (i < 50) {
                        try {
                            System.out.println(Thread.currentThread().getName()+"睡眠1s");
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    return k;
                })
                .log();
        StepVerifier.create(flux)
                .expectNextCount(100)
                .verifyComplete();
    }
}
