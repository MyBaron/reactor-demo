package com.example.reactive.demo;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;

/**
 * demo1
 *
 * @author: baron
 * @date: 2020-05-31 09:05
 **/
public class Demo1 {

    @DisplayName("发布者和订阅者的关系")
    @Test
    public void demo1() throws InterruptedException {

        //创建发布者
        SubmissionPublisher<Integer> publisher = new SubmissionPublisher<>();

        //创建订阅者
        Flow.Subscriber<Integer> subscriber = new Flow.Subscriber<>() {

            private Flow.Subscription subscription;

            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                this.subscription = subscription;
                this.subscription.request(10);
            }

            @Override
            public void onNext(Integer item) {
                System.out.println("next:" + item);
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                this.subscription.request(10);

            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
            }

            @Override
            public void onComplete() {
                System.out.println("完成了");
            }
        };


        //发布者添加订阅者
        publisher.subscribe(subscriber);

        for (int i = 0; i < 1000; i++) {
            System.out.println("添加发布事件"+i);
            publisher.submit(i);
        }

        System.out.println("发布者即将关闭");

        //发布者关闭
        publisher.close();


        Thread.currentThread().join(1000);


        System.out.println("完成");


    }

    @Test
    public void test2() throws InterruptedException {

        SubmissionPublisher<Integer> publisher = new SubmissionPublisher<>();

        //创建处理过程
        MyProcessor myProcessor = new MyProcessor();

        //添加处理过程
        publisher.subscribe(myProcessor);

        //创建订阅者
        Flow.Subscriber<String> subscriber = new Flow.Subscriber<>() {

            private Flow.Subscription subscription;

            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                this.subscription = subscription;
                this.subscription.request(1);
            }

            @Override
            public void onNext(String item) {
                System.out.println("next:" + item);

                this.subscription.request(1);

            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
            }

            @Override
            public void onComplete() {
                System.out.println("完成了");
            }
        };

        myProcessor.subscribe(subscriber);


        publisher.submit(-111);
        publisher.submit(111);

        System.out.println("生产者正在关闭");
        publisher.close();

        Thread.currentThread().join(100000000);


    }


    public class MyProcessor extends SubmissionPublisher<String> implements Flow.Processor<Integer,String>{

        private Flow.Subscription subscription;

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            this.subscription = subscription;
            this.subscription.request(10);
        }

        @Override
        public void onNext(Integer item) {
            System.out.println("Processor,next:" + item);
            if (item>0){
                this.submit("大于0的数 " + item);
            }
            this.subscription.request(10);

        }

        @Override
        public void onError(Throwable throwable) {
            throwable.printStackTrace();
        }

        @Override
        public void onComplete() {
            System.out.println("Processor,完成了");
        }
    }

}

