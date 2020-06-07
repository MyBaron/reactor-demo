package com.example.reactive.demo;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.Scheduled;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;
import reactor.util.function.Tuple3;

import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

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
        //创建持续增长的Flux,获取前10个
        Flux<Long> interval = Flux.interval(Duration.ofSeconds(1)).take(10);
        interval.log().subscribe();


        Thread.currentThread().join(1000000);

    }

    @DisplayName("zip操作符")
    @Test
    public void test3() throws InterruptedException {
        Flux.zip(
                getZipDescFlux(),
                Flux.interval(Duration.ofMillis(200))
        ).subscribe(t -> System.out.println(t.getT1()));

        Thread.currentThread().join(10000);

    }

    private Flux<String> getZipDescFlux() {
        String desc = "Zip two sources together, that is to say wait for all the sources to emit one element and combine these elements once into a Tuple2.";
        return Flux.fromArray(desc.split("\\s+"));  // 1
    }


    @Test
    public void MonoTest() {
        Mono.just(1)
                .map(integer -> "foo" + integer)
//                .or(Mono.delay(Duration.ofMillis(100)))
                .subscribe(System.out::println);

        //创建一个空的Mono
        Mono<Object> empty = Mono.empty();
        //创建一个没有任何东西，并且连完成时间也没有的Mono
        Mono<Object> never = Mono.never();
        //创建一个包含foo元素的Mono
        Mono<String> foo = Mono.just("foo");
        //创建一个异常的Mono
        Mono<Object> error = Mono.error(new IllegalStateException());

    }

    @Test
    public void stepVerifierTest() {
        // 验证Flux流有两个元素 foo,bar
        StepVerifier.create(Flux.just("foo", "bar")).expectNext("foo", "bar").verifyComplete();
        // 验证Flux流有两个元素 foo,bar，然后有一个RuntimeException error
        StepVerifier.create(Flux.just("foo", "bar").doOnError(k -> new RuntimeException())).expectNext("foo", "bar").expectError(RuntimeException.class);
        // 验证User对象的Flux
        StepVerifier.create(Flux.just(new User("swhite"), new User("jpinkman"))).expectNext(new User("swhite"), new User("jpinkman")).verifyComplete();
        StepVerifier
                .create(Flux.just(new User("swhite"), new User("jpinkman")))
                .assertNext(k -> Assertions.assertEquals("swhite", k.getUsername()))
                .assertNext(k -> Assertions.assertEquals("jpinkman", k.getUsername())).verifyComplete();
        StepVerifier
                .create(Flux.just(new User("swhite"), new User("jpinkman")))
                .assertNext(k -> org.assertj.core.api.Assertions.assertThat(k.getUsername()).isEqualTo("swhite"))
                .assertNext(k -> org.assertj.core.api.Assertions.assertThat(k.getUsername()).isEqualTo("jpinkman"))
                .verifyComplete();

        // 等待延迟事件的完成
        StepVerifier.create(Flux.just(1,2,3,4,5,6,7,8,9,10)
                .delayElements(Duration.ofMillis(100)))
                .thenAwait()
                .expectNextCount(10)
                .verifyComplete();

        //测试延迟事件产生的数据流
        StepVerifier.withVirtualTime(() -> Flux.interval(Duration.ofSeconds(1)).take(3600))
                .thenAwait(Duration.ofSeconds(3600))
                .expectNextCount(3600)
                .verifyComplete();
    }


    @Test
    public void TransformTest() {
        StepVerifier
                .create(Mono.<Person>just(new Person("username", "firstname", "lastname"))
                        .map(k -> {
                            k.setFirstname(k.getFirstname().toUpperCase());
                            k.setLastname(k.getLastname().toUpperCase());
                            k.setUsername(k.getUsername().toUpperCase());
                            return k;
                        }))
                .expectNext(new Person("USERNAME", "FIRSTNAME", "LASTNAME"))
                .verifyComplete();

        StepVerifier.create(Flux.just(new Person("username1", "firstname1", "lastname1")
                , new Person("username2", "firstname2", "lastname2"))
                .flatMap(k -> Mono.just(k.setFirstname("u" + k.getFirstname()))))
                .expectNext(new Person("username1", "ufirstname1", "lastname1"))
                .expectNext(new Person("username2", "ufirstname2", "lastname2"))
                .verifyComplete();

    }


    @Test
    public void mergeTest() {
        //并行合拼数据源 merge
        StepVerifier.create(Flux.merge(Flux.just("123", "345"), Flux.just("678", "90")).log())
                .expectNext("123", "345", "678", "90")
                .verifyComplete();
        //按顺序合并数据源 concat
        StepVerifier.create(Flux.concat(Flux.just("123", "345"), Flux.just("678", "90")))
                .expectNext("123", "345", "678", "90")
                .verifyComplete();
        //合并数据源，将两个Mono的单一源转换成Flux多元素源
        StepVerifier.create(Flux.concat(Mono.just("123"), Mono.just("345")))
                .expectNext("123", "345")
                .verifyComplete();



    }

    @Test
    public void requestTest() {
        StepVerifier.withVirtualTime(()->Flux.interval(Duration.ofSeconds(1)).take(4))
                .thenAwait(Duration.ofSeconds(4))
                .thenRequest(4)
                .expectNextCount(4)
                .verifyComplete();

        // 按顺序获取元素，Flux.just(xx,long n):n 是每次处理的元素个数，如果要获取下n个元素，需要用thenRequest()方法
        StepVerifier.create(Flux.just(RequestPerson.SKYLER, RequestPerson.JESSE, RequestPerson.JESSE), 1)
                .expectNext(RequestPerson.SKYLER)
                .thenRequest(2)
                .expectNext(RequestPerson.JESSE)
                .expectNext(RequestPerson.JESSE)
                .thenCancel()
                .verify();

        //打印日志
        StepVerifier.create(Flux.just(RequestPerson.SKYLER, RequestPerson.JESSE, RequestPerson.JESSE).log(), 1)
                .expectNext(RequestPerson.SKYLER)
                .thenRequest(2)
                .expectNext(RequestPerson.JESSE)
                .expectNext(RequestPerson.JESSE)
                .thenCancel()
                .verify();

        //执行某些操作，但是不影响到数据源，这种行为数据剽窃，doOnXX 开头的方法

        //订阅的时候输出OK
        StepVerifier.create(Flux.concat(Mono.just("123"), Mono.just("345").doOnSubscribe(k->System.out.println("OK"))))
                .expectNext("123", "345")
                .verifyComplete();
    }

    @DisplayName("错误信息处理")
    @Test
    public void errorTest() throws GetOutOfHereException, InterruptedException {

//        StepVerifier.create(Mono.error(RuntimeException::new).log())
//                .expectErrorMatches(throwable -> throwable instanceof RuntimeException)
//                .verify();

        //异常信息处理
        Flux.just(1, 2, 3)
                .flatMap(k -> {
                    if (k == 3) {
                        return Mono.error(GeneralSecurityException::new);
                    } else {
                        return Mono.just(k);
                    }
                }).onErrorResume(throwable -> Mono.just(-11))
                .log()
                .subscribeOn(Schedulers.elastic())
                .subscribe(j->{
                    System.out.println(Thread.currentThread().getName()+"  "+j);
                });

        Thread.sleep(1000);


    }


    @DisplayName("")
    @Test
    public void otherOperationsTest() {
        //zip操作
        Flux<Integer> just = Flux.just(1, 2,9);
        Flux<Integer> just1 = Flux.just(3, 4);
        Flux<Integer> just2 = Flux.just(5, 6);
        StepVerifier.create(Flux.zip(just, just1, just2).log())
                .assertNext(k -> {
                    Assertions.assertEquals(k.getT1(),1);
                    Assertions.assertEquals(k.getT2(),3);
                    Assertions.assertEquals(k.getT3(),5);
                }).assertNext(k -> {
                    Assertions.assertEquals(k.getT1(),2);
                    Assertions.assertEquals(k.getT2(),4);
                    Assertions.assertEquals(k.getT3(),6);
                })
                .verifyComplete();

        // first
        StepVerifier.create(Flux.first(just, just1, just2).log())
                .expectNextCount(3)
                .verifyComplete();

        //then
        StepVerifier.create(just.then().log())
                .expectNextCount(0)
                .verifyComplete();


    }

    @DisplayName("block阻塞")
    @Test
    public void blockTest() {
        Mono<String> just = Mono.just("123");
        String block = just.block();
        Assertions.assertEquals("123",block);
    }

    @DisplayName("defer")
    @Test
    public void deferTest() throws InterruptedException {
        System.out.println(System.currentTimeMillis());
        Mono<Long> clock = Mono.defer(() -> Mono.just(System.currentTimeMillis()));
        Thread.sleep(10_000);
        System.out.println(clock.block()); //invoked currentTimeMillis() here and returns t10
        Thread.sleep(7_000);
        System.out.println(clock.block());

        System.out.println(System.currentTimeMillis());
        Mono<Long> clock1 =  Mono.just(System.currentTimeMillis());
        Thread.sleep(10_000);
        System.out.println(clock1.block()); //invoked currentTimeMillis() here and returns t10
        Thread.sleep(7_000);
        System.out.println(clock1.block());
    }


    RequestPerson capitalizeUser(RequestPerson user) throws GetOutOfHereException {
        if (user.equals(RequestPerson.SKYLER)) {
            throw new GetOutOfHereException();
        }
        System.out.println("123");
        return new RequestPerson();
    }

    protected final class GetOutOfHereException extends Exception {
        private static final long serialVersionUID = 0L;
    }

    public static class RequestPerson{
        private static RequestPerson SKYLER = new RequestPerson();
        private static RequestPerson JESSE = new RequestPerson();

    }

    public static class Person{
        private String username;
        private String firstname;
        private String lastname;

        public Person(String username, String firstname, String lastname) {
            this.username = username;
            this.firstname = firstname;
            this.lastname = lastname;
        }

        public String getUsername() {
            return username;
        }

        public Person setUsername(String username) {
            this.username = username;
            return this;
        }

        public String getFirstname() {
            return firstname;
        }

        public Person setFirstname(String firstname) {
            this.firstname = firstname;
            return this;
        }

        public String getLastname() {
            return lastname;
        }

        public Person setLastname(String lastname) {
            this.lastname = lastname;
            return this;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Person person = (Person) o;
            return Objects.equals(username, person.username) &&
                    Objects.equals(firstname, person.firstname) &&
                    Objects.equals(lastname, person.lastname);
        }

        @Override
        public int hashCode() {
            return Objects.hash(username, firstname, lastname);
        }
    }





    public static class User {
        private String username;

        public User(String username) {
            this.username = username;
        }

        public String getUsername() {
            return username;
        }

        public User setUsername(String username) {
            this.username = username;
            return this;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            User user = (User) o;
            return Objects.equals(username, user.username);
        }

        @Override
        public int hashCode() {
            return Objects.hash(username);
        }
    }

}
