package com.melancholia.worker.worker;

import com.melancholia.worker.enums.WorkerStatusEnum;
import org.springframework.stereotype.Component;

@Component
public class WorkerStateService {

    private WorkerStatusEnum workerStatusEnum = WorkerStatusEnum.UNINITIALIZED;

    public WorkerStatusEnum getWorkerStatusEnum() {
        return workerStatusEnum;
    }

    public void setWorkerStatusEnum(WorkerStatusEnum workerStatusEnum) {
        this.workerStatusEnum = workerStatusEnum;
    }

}
