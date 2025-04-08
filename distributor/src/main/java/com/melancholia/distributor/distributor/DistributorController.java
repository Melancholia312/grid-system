package com.melancholia.distributor.distributor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.melancholia.distributor.core.ApiResponse;
import com.melancholia.distributor.dto.TaskDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
public class DistributorController {

    @Autowired
    private DistributorService distributorService;
    @Autowired
    private DistributorJarService distributorJarService;
    @Autowired
    private TaskManager taskManager;
    @Value("${upload-path}")
    private String UPLOAD_PATH;
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(DistributorController.class);

    @GetMapping("/start")
    public ResponseEntity<ApiResponse> startTask(@RequestParam("zipName") String zipName) {

        if (!isZipFileValid(zipName)) {
            ApiResponse response = new ApiResponse("No such file exists", HttpStatus.NOT_FOUND.value(), null);
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        log.info("Received new task. ZipName: {}", zipName);
        distributorService.startTask(zipName);

        ApiResponse response = new ApiResponse("Task started", HttpStatus.OK.value(), null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public boolean isZipFileValid(String zipName) {
        try {
            if (zipName == null || zipName.isEmpty()) {
                return false;
            }

            Path filePath = Paths.get(UPLOAD_PATH, zipName);
            return Files.exists(filePath)
                    && Files.isRegularFile(filePath)
                    && zipName.toLowerCase().endsWith(".zip");
        } catch (Exception e) {
            log.error("Error checking zip file {}: {}", zipName, e.getMessage());
            return false;
        }
    }

    @PostMapping("/result")
    public ResponseEntity<ApiResponse> receiveResult(@RequestBody ObjectNode result) throws JsonProcessingException {
        if (distributorService.getZipName() == null) {
            ApiResponse response = new ApiResponse("Global task already completed", HttpStatus.OK.value(), null);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        JsonNode resultNode = result.path("result");

        TaskDTO taskDTO = mapper.treeToValue(result.path("task"), TaskDTO.class);

        taskManager.removeCompetedTask(taskDTO);

        boolean processResult = (boolean) distributorJarService.executeProcessResult(resultNode.toString());
        if (processResult ||
                taskManager.getFinalEnd().equals(new BigInteger(taskDTO.getCount()))) distributorService.stopGlobalTask();

        ApiResponse response = new ApiResponse("Result received successfully", HttpStatus.OK.value(), null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
