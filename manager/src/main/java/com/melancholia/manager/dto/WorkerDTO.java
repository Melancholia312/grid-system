package com.melancholia.manager.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.melancholia.manager.enums.WorkerStatusEnum;

import java.util.Objects;

public class WorkerDTO {

    private final String host;
    private final int port;
    private WorkerStatusEnum workerStatus;
    private static final String CHECK_STATE_ENDPOINT = "check-state";

    public WorkerDTO(String host, int port) {
        this.host = host;
        this.port = port;
        workerStatus = WorkerStatusEnum.UNINITIALIZED;
    }

    public String fullAddress() {
        return String.format("http://%s:%d", host, port);
    }

    public String checkStateAddress() {
        return String.format("%s/%s", fullAddress(), CHECK_STATE_ENDPOINT);
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public WorkerStatusEnum getWorkerStatus() {
        return workerStatus;
    }

    public void setWorkerStatus(WorkerStatusEnum workerStatus) {
        this.workerStatus = workerStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkerDTO workerDTO = (WorkerDTO) o;
        return port == workerDTO.port && Objects.equals(host, workerDTO.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port);
    }
}
