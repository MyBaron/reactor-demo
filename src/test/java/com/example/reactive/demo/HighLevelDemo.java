package com.example.reactive.demo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.*;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author lanruqi
 * @date 2020/6/3
 */
@DisplayName("进阶用法")
public class HighLevelDemo {


    @DisplayName("generate用法")
    @Test
    public void generateTest() {
        final AtomicInteger count = new AtomicInteger(1);
        Flux<Object> generate = Flux.generate(sink -> {
            sink.next(count.get() + ":" + System.currentTimeMillis());
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (count.getAndIncrement() >= 5) {
                sink.complete();
            }

        }).log();
        StepVerifier.create(generate)
                .thenAwait()
                .expectNextCount(5)
                .verifyComplete();

        System.out.println("-----------------");
        Flux<Object> generate1 = Flux.generate(
                () -> 1,   //初始化传入参数
                (count1, sink) -> {
                    sink.next(count + ":" + System.currentTimeMillis());
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (count1 >= 5) {
                        sink.complete();
                    }
                    return count1 + 1;
                }
        ).log();
        StepVerifier.create(generate1)
                .thenAwait()
                .expectNextCount(5)
                .verifyComplete();

        System.out.println("-----------------");

        Flux<Object> generate2 = Flux.generate(
                () -> 1,   //初始化传入参数
                (count1, sink) -> {
                    sink.next(count + ":" + System.currentTimeMillis());
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (count1 >= 5) {
                        sink.complete();
                    }
                    return count1 + 1;
                }
        ,k->Assertions.assertEquals(5,k)) //Consumer 数据流执行完成后执行该函数
                .log();
        StepVerifier.create(generate1)
                .thenAwait()
                .expectNextCount(5)
                .verifyComplete();
    }


    @DisplayName("create测试")
    @Test
    public void createTest() throws InterruptedException {
        MyEventSource myEventSource = new MyEventSource();
        Flux<Object> objectFlux = Flux.create(fluxSink -> {
            myEventSource.register(new MyEventListener() {
                @Override
                public void onNewEvent(MyEventSource.MyEvent event) {
                    fluxSink.next(event);
                }

                @Override
                public void onEventStopped() {
                    fluxSink.complete();
                }
            });
        }).log();

        StepVerifier.create(objectFlux)
                .expectSubscription()
                //异步去跑任务
                .then(()->{
                    for (int i = 0; i < 20; i++) {
                        Random random = new Random();
                        try {
                            TimeUnit.MICROSECONDS.sleep(random.nextInt(1000));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        myEventSource.newEvent(new MyEventSource.MyEvent(new Date(), "Event-" + i));
                    }
                    myEventSource.eventStopped();
                })
                .expectNextCount(20)
                .verifyComplete();
    }


    private CountDownLatch countDownLatch;
    private final int EVENT_DURATION   = 10;    // 生成的事件间隔时间，单位毫秒
    private final int EVENT_COUNT      = 20;    // 生成的事件个数
    private final int PROCESS_DURATION = 30;    // 订阅者处理每个元素的时间，单位毫秒
    @BeforeEach
    public void setup() {
        countDownLatch = new CountDownLatch(1);
    }

    @DisplayName("回压策略测试")
    @Test
    public void strategyTest() throws InterruptedException {
        MyEventSource myEventSource = new MyEventSource();
        //测试慢订阅者的情况下 采取
        /**
         * ERROR： 当下游跟不上节奏的时候发出一个错误信号。
         * DROP：当下游没有准备好接收新的元素的时候抛弃这个元素。
         * LATEST：让下游只得到上游最新的元素。
         * BUFFER：缓存下游没有来得及处理的元素（如果缓存不限大小的可能导致OutOfMemoryError）。
         */

        Flux<MyEventSource.MyEvent> flux = createFlux(FluxSink.OverflowStrategy.LATEST, myEventSource)
                .doOnRequest(k-> System.out.println("======== request:"+k+"======"))
//                .publishOn(Schedulers.newSingle("newSingle"),1);
                .publishOn(Schedulers.elastic(),1);
        flux.subscribe(new SlowSubscriber());
        generateEvent(EVENT_COUNT,EVENT_DURATION,myEventSource);
        countDownLatch.await();

    }




    private Flux<MyEventSource.MyEvent> createFlux(FluxSink.OverflowStrategy strategy,MyEventSource myEventSource) {
        return Flux.create(sink -> myEventSource.register(new MyEventListener() {
            @Override
            public void onNewEvent(MyEventSource.MyEvent event) {
                System.out.println(Thread.currentThread().getName()+ "publish >>> " + event.getMessage());
                sink.next(event);
            }

            @Override
            public void onEventStopped() {
                sink.complete();
            }
        }), strategy);
    }

    private void generateEvent(int times,int millis,MyEventSource myEventSource) {
        // 循环生成MyEvent，每个MyEvent间隔millis毫秒
        for (int i = 0; i < times; i++) {
            try {
                TimeUnit.MILLISECONDS.sleep(millis);
            } catch (InterruptedException e) {
            }
            myEventSource.newEvent(new MyEventSource.MyEvent(new Date(), "Event-" + i));
        }
        myEventSource.eventStopped();
    }




    public interface MyEventListener {
        void onNewEvent(MyEventSource.MyEvent event);
        void onEventStopped();
    }

    // 事件源
    public static class MyEventSource{
        private List<MyEventListener> listeners;
        public MyEventSource() {
            this.listeners = new ArrayList<>();
        }

        public void register(MyEventListener listener) {    // 1
            listeners.add(listener);
        }

        public void newEvent(MyEvent event) {
            for (MyEventListener listener :
                    listeners) {
                listener.onNewEvent(event);     // 2
            }
        }

        public void eventStopped() {
            for (MyEventListener listener :
                    listeners) {
                listener.onEventStopped();      // 3
            }
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class MyEvent {   // 4
            private Date timeStemp;
            private String message;
        }

    }


    public class SlowSubscriber extends BaseSubscriber<MyEventSource.MyEvent> {
        @Override
        protected void hookOnSubscribe(Subscription subscription) {
            // 订阅的时候请求1个参数
            request(1);
        }

        @Override
        protected void hookOnNext(MyEventSource.MyEvent event) {
            System.out.println(Thread.currentThread().getName()+  "                      receive <<< " + event.getMessage());
            try {
                TimeUnit.MILLISECONDS.sleep(PROCESS_DURATION);
            } catch (InterruptedException e) {
            }
            request(3);     // 每处理完1个数据，就再请求1个
        }

        @Override
        protected void hookOnComplete() {
            countDownLatch.countDown();
        }

        @Override
        protected void hookOnError(Throwable throwable) {
            System.err.println("                      receive <<< " + throwable);
        }
    }
}
