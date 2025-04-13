package com.melancholia.worker.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.melancholia.worker.ApplicationStartup;
import com.melancholia.worker.core.ApiResponse;
import com.melancholia.worker.dto.ManifestDTO;
import com.melancholia.worker.dto.TaskDTO;
import com.melancholia.worker.enums.WorkerStatusEnum;
import com.melancholia.worker.utils.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@RestController
public class WorkerController {

    @Autowired
    private WorkerService workerService;
    @Autowired
    private SolverService solverService;
    @Autowired WorkerStateService workerStateService;
    @Value("${upload-path}")
    private String UPLOAD_PATH;

    public static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(ApplicationStartup.class);

    @PostMapping("/init")
    public ResponseEntity<ApiResponse> init(@RequestPart("jarFile") MultipartFile jarFile,
                                            @RequestPart("archiveFile") MultipartFile archiveFile,
                                            @RequestPart("jsonData") MultipartFile jsonData){

        Path uploadDir = Paths.get(UPLOAD_PATH);

        try {
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            Path jarFilePath = uploadDir.resolve(jarFile.getOriginalFilename());
            Files.copy(jarFile.getInputStream(), jarFilePath, StandardCopyOption.REPLACE_EXISTING);

            Path archiveFilePath = uploadDir.resolve(archiveFile.getOriginalFilename());
            Files.copy(archiveFile.getInputStream(), archiveFilePath, StandardCopyOption.REPLACE_EXISTING);

            Path jsonFilePath = uploadDir.resolve(jsonData.getOriginalFilename());
            Files.copy(jsonData.getInputStream(), jsonFilePath, StandardCopyOption.REPLACE_EXISTING);

            File jsonFile = jsonFilePath.toFile();
            ManifestDTO manifestDTO = mapper.readValue(jsonFile, ManifestDTO.class);

            solverService.setSolveMethod(ReflectionUtils.getAnnotatedMethodsByName(
                    jarFilePath,
                    manifestDTO.getClassName(),
                    manifestDTO.getAnnotationName()).get(0));
            solverService.setZipPath(archiveFilePath);
            workerStateService.setWorkerStatusEnum(WorkerStatusEnum.FREE);
            log.info("Success init");

            ApiResponse response = new ApiResponse("Success init", HttpStatus.OK.value(), null);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
            ApiResponse response = new ApiResponse("Error occurred", HttpStatus.INTERNAL_SERVER_ERROR.value(), null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/solve")
    public ResponseEntity<ApiResponse> solve(@RequestBody TaskDTO taskDTO) {
        log.info("Received task: {}:{}", taskDTO.getStart(), taskDTO.getCount());
        workerService.solve(taskDTO);

        ApiResponse response = new ApiResponse("Task received", HttpStatus.OK.value(), null);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @GetMapping("/check-state")
    public ResponseEntity<ApiResponse> checkWorkerState() {
        WorkerStatusEnum workerStatusEnum = workerStateService.getWorkerStatusEnum();

        ApiResponse response = new ApiResponse("Worker state", HttpStatus.OK.value(), workerStatusEnum);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/reset")
    public ResponseEntity<ApiResponse> reset() {
        workerService.reset();

        ApiResponse response = new ApiResponse("Reset successfully", HttpStatus.OK.value(), null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
