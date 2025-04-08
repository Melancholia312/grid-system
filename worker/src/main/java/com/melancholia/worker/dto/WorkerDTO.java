package com.melancholia.worker.dto;

import java.util.Objects;

public class WorkerDTO {

    private String host;
    private int port;

    public WorkerDTO(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String fullAddress() {
        return String.format("http://%s:%d", host, port);
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
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
