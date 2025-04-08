package com.melancholia.worker.worker;

import com.melancholia.worker.dto.TaskDTO;
import com.melancholia.worker.utils.ReflectionUtils;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.nio.file.Path;

@Service
public class SolverService {

    private Method solveMethod = null;
    private Path zipPath = null;

    public void setSolveMethod(Method solveMethod) {
        this.solveMethod = solveMethod;
    }

    public void setZipPath(Path zipPath) {
        this.zipPath = zipPath;
    }

    public Object solve(TaskDTO taskDTO) {
        Object[] solveArgs = {zipPath, taskDTO.getStart(), taskDTO.getCount()};
        return ReflectionUtils.executeMethod(solveMethod, solveArgs);
    }

}
