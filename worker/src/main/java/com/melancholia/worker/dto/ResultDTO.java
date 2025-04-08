package com.melancholia.worker.dto;

public class ResultDTO {
    private final TaskDTO taskDTO;
    private final String result;

    public ResultDTO(TaskDTO taskDTO, String result) {
        this.taskDTO = taskDTO;
        this.result = result;
    }

}
