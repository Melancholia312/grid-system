package com.melancholia.manager.manager;

import com.melancholia.manager.core.ApiResponse;
import com.melancholia.manager.dto.WorkerDTO;
import com.melancholia.manager.enums.WorkerStatusEnum;
import com.melancholia.manager.utils.HttpSenderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
public class ManagerService {

    private final Set<WorkerDTO> workerList = new HashSet<>();
    private static final long HEALTH_CHECK_INTERVAL_MS = 5_000;

    @Autowired
    private HttpSenderService httpSenderService;
    @Autowired
    private Executor executor;
    @Autowired
    private TaskScheduler taskScheduler;

    private static final Logger log = LoggerFactory.getLogger(HttpSenderService.class);

    public void registerWorker(WorkerDTO worker){
        log.info("Added new worker {}:{}. Remaining workers: {}", worker.getHost(), worker.getPort(), workerList.size());
        workerList.add(worker);
    }

    public void leaveWorker(WorkerDTO worker) {
        log.info("Worker leaved {}:{}. Remaining workers: {}", worker.getHost(), worker.getPort(), workerList.size());
        workerList.remove(worker);
    }

    public Set<WorkerDTO> getWorkers(){
        return workerList;
    }

    @Scheduled(fixedRate = HEALTH_CHECK_INTERVAL_MS)
    public void checkWorkersStatus() {
        Flux.fromIterable(workerList)
                .flatMap(worker ->
                        httpSenderService.sendGetRequest(worker.checkStateAddress(), ApiResponse.class)
                                .timeout(Duration.ofSeconds(3))
                                .map(apiResponse -> {
                                    worker.setWorkerStatus(
                                            WorkerStatusEnum.valueOf(apiResponse.getData().toString()));
                                    return worker;
                                })
                                .onErrorResume(error -> {
                                    log.error("Worker {} check failed: {}", worker.fullAddress(), error.getMessage());
                                    leaveWorker(worker);
                                    return Mono.just(worker);
                                })
                                .subscribeOn(Schedulers.boundedElastic())
                )
                .subscribe(
                        null,
                        error -> log.error("Critical error in worker check: {}", error.getMessage())
                );
    }

}
