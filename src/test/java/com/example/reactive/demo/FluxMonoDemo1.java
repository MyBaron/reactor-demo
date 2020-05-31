package com.example.reactive.demo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

/**
 * @author: baron
 * @date: 2020-05-31 16:11
 **/
public class FluxMonoDemo1 {



    @Test
    public void test1() {
        Flux<String> stringFlux = Flux.fromArray(new String[]{"a", "b", "c"});
        stringFlux.subscribe(System.out::println);

        Mono<String> monoDemo = Mono.fromFuture(CompletableFuture.completedFuture("hello world"));
        monoDemo.subscribe(System.out::println);


        Flux.merge(
                Flux.interval(Duration.ofMillis(100))
                .map(v -> "a" + v),Flux.interval(Duration.ofSeconds(1),
                        Duration.ofMillis(10))
                        .map(v -> "b" + v))
                .bufferTimeout(10, Duration.ofMillis(500))
                .take(3)
                .toStream()
                .forEach(System.out::println);


        System.out.println("---------");
        Flux.just(1, 2, 3)
                .concatMap(v -> Flux.interval(Duration.ofMillis(100))
                        .take(v))
                .toStream()
                .forEach(System.out::println);

        System.out.println("---------");
        Flux.interval(Duration.ofMillis(100))
                .take(1)
                .toStream()
                .forEach(System.out::println);

        System.out.println("---------");
        Flux.just(1,2,3)
                .flatMap(k->Flux.just(k*10,k*100,k*1000))
                .toStream()
                .forEach(System.out::println);


    }


    @Test
    @DisplayName("创建Flux")
    public void test2() throws InterruptedException {
        Flux.fromIterable(Arrays.asList(999, 1000))
                .delayElements(Duration.ofMillis(100))
                .doOnNext(k -> System.out.println("netx: " + k))
                .map(k -> k * 2)
                .take(10)
                .subscribe(System.out::println);

        // 空的flux
        Flux<Object> empty = Flux.empty();
        //自定义数据的flux
        Flux<String> just = Flux.just("foo","bar");
        //从遍历器中构建flux
        Flux<String> stringFlux = Flux.fromIterable(Arrays.asList("foo", "bar"));
        //异常抛出
        Flux<Object> error = Flux.error( new IllegalStateException("error"));


        Thread.currentThread().join(10000);

    }
}
