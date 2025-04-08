package com.melancholia.worker;

import com.melancholia.worker.core.ApiResponse;
import com.melancholia.worker.dto.WorkerDTO;
import com.melancholia.worker.utils.HttpSenderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;


@Component
public class ApplicationStartup {

    @Autowired
    private HttpSenderService httpSenderService;
    @Autowired
    private String managerAddress;
    @Value("${server.port}")
    private int port;
    private static final String REGISTER_ENDPOINT = "worker-register";
    private static final String LEAVE_ENDPOINT = "worker-leave";

    private static final Logger log = LoggerFactory.getLogger(ApplicationStartup.class);

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationStartEvent(ApplicationReadyEvent event) {
        String url = String.format("%s/%s", managerAddress, REGISTER_ENDPOINT);

        Mono.fromCallable(() -> new WorkerDTO(
                        InetAddress.getLocalHost().getHostAddress(),
                        port))
                .onErrorMap(UnknownHostException.class, e -> {
                    log.error("Failed to determine local host address", e);
                    return new RuntimeException("Couldn't resolve local host address", e);
                })
                .flatMap(workerDTO -> httpSenderService.sendPostRequest(url, workerDTO, ApiResponse.class))
                .timeout(Duration.ofSeconds(5))
                .retryWhen(Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(1)))
                .doOnSubscribe(sub -> log.info("Attempting to register with manager at {}", url))
                .subscribe(
                        response -> log.info("Successfully registered with manager: {}", response.getMessage()),
                        error -> {
                            log.error("Failed to register with manager: {}", error.getMessage());
                            throw new RuntimeException("Registration failed", error);
                        },
                        () -> log.debug("Registration process completed")
                );
    }

    @EventListener
    public void onApplicationEndEvent(ContextClosedEvent event) {
        String url = String.format("%s/%s", managerAddress, LEAVE_ENDPOINT);
        log.info("Attempting to unregister from manager at {}", url);

        Mono.fromCallable(() -> new WorkerDTO(
                        InetAddress.getLocalHost().getHostAddress(),
                        port))
                .onErrorResume(e -> {
                    log.error("Failed to resolve local host address", e);
                    return Mono.error(new RuntimeException("Couldn't resolve local address", e));
                })
                .flatMap(workerDTO -> httpSenderService.sendPostRequest(url, workerDTO, ApiResponse.class))
                .doOnSuccess(response ->
                        log.info("Successfully unregistered: {}", response.getMessage()))
                .doOnError(error ->
                        log.error("Failed to unregister from manager", error))
                .subscribe(
                        null,
                        error -> log.error("Critical unregistration failure", error),
                        () -> log.info("Unregistration process completed")
                );
    }

}
