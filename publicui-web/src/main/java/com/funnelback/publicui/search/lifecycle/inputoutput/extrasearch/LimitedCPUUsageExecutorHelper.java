package com.funnelback.publicui.search.lifecycle.inputoutput.extrasearch;

import static com.funnelback.config.keys.Keys.FrontEndKeys.ModernUI.EXTRA_SEARCH_CPU_COUNT_PERCENTAGE;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.springframework.core.task.TaskExecutor;

import com.funnelback.common.config.Keys;
import com.funnelback.config.configtypes.service.ServiceConfigReadOnly;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/**
 * This limits the number of Tasks that will be submited to the given TaskExecutor.
 * 
 * <p>Using this it is possible to ensure that only (say 4) extra search tasks are ever 
 * submitted to the global TaskExecutor for a single search at a time. This way if the server had
 * 40 cores no one search can consumer all of the available CPUs.</p>
 *  
 * <p>This works by creating a semaphore and attempting to acquire from the semaphore 
 * before submitting a task and releasing the semaphore once the extra search has completed.
 * This does mean that submit() can be a blocking operation.</p>
 *
 */
@Log4j2
public class LimitedCPUUsageExecutorHelper implements ExecutorHelper {

    private TaskExecutor taskExecutor;
    @Setter(AccessLevel.PACKAGE) @Getter(AccessLevel.PACKAGE) private Semaphore semaphore;
    private boolean preventFurtherTasks;
    
    public LimitedCPUUsageExecutorHelper(TaskExecutor taskExecutor, ServiceConfigReadOnly serviceConfig) {
        
        this.taskExecutor = taskExecutor;
        
        int numberOfProcs = getNumberOfProcsToUse(serviceConfig);
        
        log.debug("Will limit the number of concurrent extra searches to: {}", numberOfProcs);
        semaphore = new Semaphore(numberOfProcs);
        this.preventFurtherTasks = false;
    }
    
    int getNumberOfProcsToUse(ServiceConfigReadOnly serviceConfig) {
        // this may be larger than 100%, this just results in something
        // like the old behavior where more extra searches are executed than CPUs.
        double pcOfCores = serviceConfig.get(EXTRA_SEARCH_CPU_COUNT_PERCENTAGE) / 100.0D;
        log.trace("The cores to use is {}% from config option", pcOfCores, Keys.ModernUI.EXTRA_SEARCH_CPU_COUNT_PERCENTAGE);
        int numberOfProcs = (int) Math.floor((double) getNumberOfCurrentCPUs() * pcOfCores);
        return Math.max(numberOfProcs, 1);
    }
    
    int getNumberOfCurrentCPUs() {
        return Runtime.getRuntime().availableProcessors();
    }
    
    
    
    public <T> Optional<FutureTask<T>> submit(Callable<T> runnable, String extraSearchName, long msToWait) {
        
        if(this.preventFurtherTasks) {
            return Optional.empty();
        }
        
        // We wait here before scheduling the task because we don't want to fill the 
        // global TaskExecutor with tasks that are blocked on the semaphore.
        
        boolean acquiredLock = false;
        try {
            acquiredLock = semaphore.tryAcquire(msToWait, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            this.preventFurtherTasks = true;
            // It probably makes sense to re-throw I guess this would happen
            // if the search transaction is expected to stop.
            throw new RuntimeException(e);
        }
        if(!acquiredLock) {
            log.error("Could not execute extra search '{}' as we timed out waiting to queue the task. "
                + "No more extra searches will be queued for this search", extraSearchName);
            this.preventFurtherTasks = true;
            return Optional.empty();
        }
        
        try {
            // Wrap the task so that once it completes we release the semaphore.
            FutureTask<T> task = new FutureTask<>(new Callable<T>() {
                @Override
                public T call() throws Exception {
                    try {
                        return runnable.call();
                    } finally {
                        semaphore.release();
                    }
                }});
        
            taskExecutor.execute(task);
            return Optional.of(task);
        } catch (Exception e) {
            // If we could not submit the task then we release the semaphore.
            semaphore.release();
            log.error("Could not submit extra search {}", extraSearchName, e);
        }
        
        return Optional.empty();
        
        
    }
}
