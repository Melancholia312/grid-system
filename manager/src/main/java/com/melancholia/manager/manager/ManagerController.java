package com.melancholia.manager.manager;

import com.melancholia.manager.core.ApiResponse;
import com.melancholia.manager.dto.WorkerDTO;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class ManagerController {

    @Autowired
    private ManagerService managerService;

    @PostMapping("/worker-register")
    public ResponseEntity<ApiResponse> workerRegister(@RequestBody WorkerDTO request) {
        managerService.registerWorker(request);
        ApiResponse response = new ApiResponse("Worker registered successfully", HttpStatus.OK.value(), null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/worker-leave")
    public ResponseEntity<ApiResponse> workerLeave(@RequestBody WorkerDTO request) {
        managerService.leaveWorker(request);
        ApiResponse response = new ApiResponse("Worker unregistered successfully", HttpStatus.OK.value(), null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/get-workers")
    public ResponseEntity<ApiResponse> getWorkers() {
        ApiResponse response = new ApiResponse("Active workers", HttpStatus.OK.value(), managerService.getWorkers());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
