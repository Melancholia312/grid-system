package com.melancholia.manager.utils;

import com.melancholia.manager.ApplicationStartup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class HttpSenderService {

    @Autowired
    private WebClient webClient;

    private static final Logger log = LoggerFactory.getLogger(HttpSenderService.class);

    public <T> Mono<T> sendPostRequest(String url, Object data, Class<T> responseType) {
        return webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(data)
                .retrieve()
                .bodyToMono(responseType)
                .onErrorResume(e -> {
                    log.error("Error sending POST request to {}: {}", url, e.getMessage());
                    return Mono.error(e);
                });
    }

    public <T> Mono<T> sendGetRequest(String url, Class<T> responseType) {
        return webClient.get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(responseType)
                .onErrorResume(e -> {
                    log.error("Error sending GET request to {}: {}", url, e.getMessage());
                    return Mono.error(e);
                });
    }

}
