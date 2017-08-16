package com.funnelback.publicui.search.lifecycle.inputoutput.extrasearch;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public interface ExecutorHelper {

    /**
     * Arranges for the given runnable to be executed returning a FutureTask
     * 
     * @param callable The task to run.
     * @param taskName The name of the task used for loggin.
     * @param msToWait The length of time we can take to submit the task.
     * @return
     */
    public <T> Optional<FutureTask<T>> submit(Callable<T> callable, 
        String taskName, 
        long msToWait);
}
