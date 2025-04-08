package com.melancholia.worker.dto;

public class TaskDTO {

    private final String start;
    private final String count;
    private final String callbackUrl;

    public TaskDTO(String start, String count, String callbackUrl) {
        this.start = start;
        this.count = count;
        this.callbackUrl = callbackUrl;
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

}
