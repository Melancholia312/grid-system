package com.melancholia.distributor.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.net.InetAddress;
import java.util.Objects;

public class TaskDTO {

    private String start;
    private String count;
    private String callbackUrl;
    @JsonIgnore
    private String worker;

    public TaskDTO() {}

    public TaskDTO(String start, String count, String callbackUrl) {
        this.start = start;
        this.count = count;
        this.callbackUrl = callbackUrl;
        this.worker = null;
    }

    public String getStart() {
        return start;
    }

    public String getCount() {
        return count;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public String getWorker() {
        return worker;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public void setWorker(String worker) {
        this.worker = worker;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskDTO taskDTO = (TaskDTO) o;
        return Objects.equals(start, taskDTO.start) && Objects.equals(count, taskDTO.count);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, count);
    }
    
}
