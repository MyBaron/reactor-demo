package com.example.reactive.demo;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.EventsResultCallback;
import jdk.jfr.Event;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.concurrent.TimeUnit;

/**
 * @author lanruqi
 * @date 2020/6/3
 */
@DisplayName("docker监听")
public class DockerEventTest {

    @Test
    public void dockerEventToFlux() throws InterruptedException {
        collectDockerEvents().subscribe(System.out::println);
        TimeUnit.MINUTES.sleep(1);
    }

    private Flux<Event> collectDockerEvents() {
        DockerClient docker = DockerClientBuilder.getInstance("tcp://192.168.246.128:2375").build();
        return Flux.create(sink -> {
            EventsResultCallback eventsResultCallback = new EventsResultCallback() {
                @Override
                public void onNext(com.github.dockerjava.api.model.Event item) {
                    super.onNext(item);
                }
            };
            docker.eventsCmd().exec(eventsResultCallback);  // 4
        });
    }
}
