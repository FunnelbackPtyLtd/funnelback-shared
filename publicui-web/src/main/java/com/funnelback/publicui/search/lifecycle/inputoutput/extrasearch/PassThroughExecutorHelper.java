package com.funnelback.publicui.search.lifecycle.inputoutput.extrasearch;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import org.springframework.core.task.TaskExecutor;

import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
public class PassThroughExecutorHelper implements ExecutorHelper {

    @NonNull private final TaskExecutor taskExecutor;
    
    @Override
    public <T> Optional<FutureTask<T>> submit(Callable<T> callable, String taskName, long msToWait) {
        FutureTask<T> task = new FutureTask<>(callable);
        taskExecutor.execute(task);
        return Optional.of(task);
    }

}
