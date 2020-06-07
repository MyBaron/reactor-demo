package com.example.reactive.demo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.Disposable;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.UnicastProcessor;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * @author lanruqi
 * @date 2020/6/4
 */
@DisplayName("热流")
public class HotStraemDemo {


    @Test
    @DisplayName("UnicastProcessor")
    public void testUnicastProcessor() {
        UnicastProcessor<String> hotSource = UnicastProcessor.create();

        Flux<String> hotFlux = hotSource.publish().autoConnect().map(String::toUpperCase)
                .checkpoint()
                .log();
        hotFlux.subscribe(d -> System.out.println("Subscriber 1 to "+d));
        hotSource.onNext("blue");
        hotSource.onNext("green");

        hotFlux.subscribe(d -> System.out.println("Subscriber 2 to "+d));
        hotSource.onNext("yellow");
        hotSource.onNext("pink");
        hotSource.onComplete();
    }


    @Test
    @DisplayName("自动连接")
    public void testConnectableFlux() throws InterruptedException {
        Flux<Integer> source = Flux.range(1, 100)
                .delayElements(Duration.ofMillis(1000))
                .doOnSubscribe(s -> System.out.println("上游收到订阅"));

        ConnectableFlux<Integer> publish = source.publish();

        publish.subscribe(System.out::println, e -> {}, () -> {});
        publish.subscribe(System.out::println, e -> {}, () -> {});

        System.out.println("订阅者完成订阅操作");
        Thread.sleep(500);
        System.out.println("还没有连接上");

        // 当有足够的订阅接入后，可以对 flux 手动执行一次。它会触发对上游源的订阅
         publish.connect();

        Thread.sleep(100000);
    }

    @Test
    @DisplayName("订阅数达到2自动连接")
    public void testConnectableFluxAutoConnect() throws InterruptedException {
        Flux<Integer> source = Flux.range(1, 100)
                .delayElements(Duration.ofMillis(1000))
                .doOnSubscribe(s -> System.out.println("上游收到订阅"));

        Flux<Integer> autoCo = source.publish().autoConnect(2);

        autoCo.subscribe(System.out::println,e->{},()->{});
        System.out.println("第一个订阅者完成");
        Thread.sleep(500);
        autoCo.subscribe(System.out::println,e->{},()->{});
        System.out.println("第二个订阅者完成");

        Thread.sleep(10000);
    }

    @Test
    @DisplayName("检测订阅者取消连接")
    public void testRefCount() throws InterruptedException {
        Flux<Long> source = Flux.interval(Duration.ofSeconds(1))
                .doOnSubscribe(s -> System.out.println("上游收到订阅"))
                .doOnCancel(()-> System.out.println("上游发布者断开连接"));

        Flux<Long> refCounted = source.publish().refCount(2, Duration.ofSeconds(2));

        /**
         * 随着前两个订阅者相继取消订阅，第三个订阅者及时（在2秒内）开始订阅，所以上游会继续发出数据，而且根据输出可以看出是“热序列”。
         *
         * 当第三个订阅者取消后，第四个订阅者没能及时开始订阅，所以上游发布者断开连接。当第五个订阅者订阅之后，第四和第五个订阅者相当于开始了新一轮的订阅。
         */

        System.out.println("第一个订阅者订阅");
        Disposable sub1  = refCounted.subscribe(l -> System.out.println("sub1:" + l));

        TimeUnit.SECONDS.sleep(1);
        System.out.println("第二个订阅者订阅");
        Disposable sub2 = refCounted.subscribe(l -> System.out.println("sub2: " + l));

        TimeUnit.SECONDS.sleep(1);
        System.out.println("第一个订阅者取消订阅");
        sub1.dispose();

        TimeUnit.SECONDS.sleep(1);
        System.out.println("第二个订阅者取消订阅");
        sub2.dispose();

        TimeUnit.SECONDS.sleep(1);
        System.out.println("第三个订阅者订阅");
        Disposable sub3 = refCounted.subscribe(l -> System.out.println("sub3: " + l));

        TimeUnit.SECONDS.sleep(1);
        System.out.println("第三个订阅者取消订阅");
        sub3.dispose();

        TimeUnit.SECONDS.sleep(3);
        System.out.println("第四个订阅者订阅");
        Disposable sub4 = refCounted.subscribe(l -> System.out.println("sub4: " + l));
        TimeUnit.SECONDS.sleep(1);
        System.out.println("第五个订阅者订阅");
        Disposable sub5 = refCounted.subscribe(l -> System.out.println("sub5: " + l));
        TimeUnit.SECONDS.sleep(2000);

    }
}
