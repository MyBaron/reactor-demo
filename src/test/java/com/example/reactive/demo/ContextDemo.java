package com.example.reactive.demo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

/**
 * @author lanruqi
 * @date 2020/6/4
 */
@DisplayName("Context使用")
public class ContextDemo {


    @Test
    @DisplayName("Context使用")
    public void testContext() {
        String key = "message";
        Mono<String> mono = Mono.just("Hello")
                .flatMap(s -> Mono.subscriberContext().map(context -> s + " " + context.get(key)))
                .log()
                .subscriberContext(context -> context.put(key, "World"));

        StepVerifier.create(mono)
                .expectNext("Hello World")
                .verifyComplete();
    }


    @Test
    @DisplayName("创建空的Context")
    public void testContext_null() {
        String key = "message";
        Mono<String> mono = Mono.just("Hello")
                .flatMap(s -> Mono.subscriberContext().map(context -> s + " " + context.getOrDefault(key,"World")))
                .log()
                .subscriberContext(Context.empty());

        StepVerifier.create(mono)
                .expectNext("Hello World")
                .verifyComplete();
    }

    @Test
    @DisplayName("从下游向上游传递")
    public void testContext_bottom_to_top() {
        String key = "message";
        Mono<String> mono = Mono.just("Hello")
                .subscriberContext(context -> context.put(key,"World"))
                .flatMap(s -> Mono.subscriberContext().map(context -> s + " " + context.getOrDefault(key, "You")))
                .log();

        StepVerifier.create(mono)
                .expectNext("Hello You")
                .verifyComplete();
    }

    @Test
    @DisplayName("Context对象不可变")
    public void testContext_immutable() {
        String key = "message";
        Mono<String> mono = Mono.subscriberContext()
                .map(context -> context.put(key, "World")) //尝试修改它 给它赋予一个新的值，put返回的对象是新的对象与Mono.subscriberContext()的对象不是同一个
                .flatMap(context -> Mono.subscriberContext()) //再次从静态方法中获取Context对象
                .map(context -> context.getOrDefault(key, "defalue"))
                .log();

        StepVerifier.create(mono)
                .expectNext("defalue")
                .verifyComplete();
    }
}
