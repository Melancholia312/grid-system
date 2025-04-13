package com.melancholia.worker.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.melancholia.worker.ApplicationStartup;
import com.melancholia.worker.core.ApiResponse;
import com.melancholia.worker.dto.TaskDTO;
import com.melancholia.worker.enums.WorkerStatusEnum;
import com.melancholia.worker.utils.HttpSenderService;
import com.melancholia.worker.utils.ReflectionUtils;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;


@Service
public class WorkerService {
    @Autowired
    private HttpSenderService httpSenderService;
    @Autowired
    private SolverService solverService;
    @Autowired
    private WorkerStateService workerStateService;
    @Value("${upload-path}")
    private String UPLOAD_PATH;
    @Value("${temp-path}")
    private String TEMP_PATH;

    private static final Logger log = LoggerFactory.getLogger(ApplicationStartup.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    @Async
    public void solve(TaskDTO taskDTO) {
        workerStateService.setWorkerStatusEnum(WorkerStatusEnum.WORKING);
        ObjectNode result = (ObjectNode) solverService.solve(taskDTO);

        ObjectNode finalResult = JsonNodeFactory.instance.objectNode();
        finalResult.set("result", result);
        finalResult.set("task", mapper.valueToTree(taskDTO));

        Mono<ApiResponse> responseMono = httpSenderService.sendPostRequest(taskDTO.getCallbackUrl(), finalResult, ApiResponse.class);

        responseMono.subscribe(
                response -> log.info("Status code: {}. Response: {}", response.getStatusCode(), response.getMessage()),
                error -> log.error("Error: {}", error.getMessage())
        );

        log.info("Task {}:{} completed successfully. Result: {}", taskDTO.getStart(), taskDTO.getCount(), result);
        workerStateService.setWorkerStatusEnum(WorkerStatusEnum.FREE);
    }

    public void reset() {
        workerStateService.setWorkerStatusEnum(WorkerStatusEnum.UNINITIALIZED);
        solverService.setZipPath(null);
        solverService.setSolveMethod(null);
        try {
            ReflectionUtils.classLoader.close();
            FileUtils.deleteDirectory(new File(UPLOAD_PATH));
            FileUtils.deleteDirectory(new File(TEMP_PATH));
        } catch (IOException e) {
            log.error("An error occurred while deleting the directory {}", e.getMessage());
        }
        log.info("The worker was reset successfully");
    }
}
