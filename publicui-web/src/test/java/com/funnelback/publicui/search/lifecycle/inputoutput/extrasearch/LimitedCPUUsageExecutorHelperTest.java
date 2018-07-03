package com.funnelback.publicui.search.lifecycle.inputoutput.extrasearch;

import static com.funnelback.config.keys.Keys.FrontEndKeys;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.FutureTask;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import org.mockito.InOrder;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.TaskRejectedException;

import com.funnelback.config.configtypes.service.ServiceConfigReadOnly;

import lombok.Getter;

public class LimitedCPUUsageExecutorHelperTest {
    
    public class FourCoreLimitedCPUUsageExecutorHelper extends LimitedCPUUsageExecutorHelper {

        public FourCoreLimitedCPUUsageExecutorHelper(TaskExecutor taskExecutor, ServiceConfigReadOnly serviceConfig) {
            super(taskExecutor, serviceConfig);
        }
        
        @Override
        int getNumberOfCurrentCPUs() {
            return 4;
        }
        
    }

    @Test
    public void testCalculateNumberOfProcs() {
        LimitedCPUUsageExecutorHelper executor= 
            new FourCoreLimitedCPUUsageExecutorHelper(null, getConfigWithExtraSearchCpuPercent(50));
        
        Assert.assertEquals(2, executor.getSemaphore().availablePermits());
        
        Assert.assertEquals(2, executor.getNumberOfProcsToUse(getConfigWithExtraSearchCpuPercent(50))); 
        Assert.assertEquals(1, executor.getNumberOfProcsToUse(getConfigWithExtraSearchCpuPercent(-1)));
        Assert.assertEquals(1, executor.getNumberOfProcsToUse(getConfigWithExtraSearchCpuPercent(0)));
        Assert.assertEquals(1, executor.getNumberOfProcsToUse(getConfigWithExtraSearchCpuPercent(24)));
        Assert.assertEquals(1, executor.getNumberOfProcsToUse(getConfigWithExtraSearchCpuPercent(25)));
        Assert.assertEquals(1, executor.getNumberOfProcsToUse(getConfigWithExtraSearchCpuPercent(44)));
        Assert.assertEquals(3, executor.getNumberOfProcsToUse(getConfigWithExtraSearchCpuPercent(76)));
        Assert.assertEquals(3, executor.getNumberOfProcsToUse(getConfigWithExtraSearchCpuPercent(99)));
        Assert.assertEquals(4, executor.getNumberOfProcsToUse(getConfigWithExtraSearchCpuPercent(100)));
        
        // We support overallocation 
        Assert.assertEquals(8, executor.getNumberOfProcsToUse(getConfigWithExtraSearchCpuPercent(200)));
    }
    
    @Test
    public void testLimitedBySemaphore() throws Exception {
        SyncTaskExecutor taskExecutor = spy(new SyncTaskExecutor());
        LimitedCPUUsageExecutorHelper executor= 
            new FourCoreLimitedCPUUsageExecutorHelper(taskExecutor, getConfigWithExtraSearchCpuPercent(0));
        
        // Setup a dummy semaphore.
        Semaphore semaphore = mock(Semaphore.class);
        when(semaphore.tryAcquire(12, TimeUnit.MILLISECONDS)).thenReturn(true);
        executor.setSemaphore(semaphore);
        
        
        Optional<FutureTask<String>> task = executor.submit(() -> {
            // Call this during execution so we can check that the semaphore
            // is released after the task.
            semaphore.availablePermits();
            return "";
        }, "name", 12);
        
        Assert.assertTrue(task.isPresent());
        
        InOrder order = inOrder(semaphore, taskExecutor);
        // First try to take the semaphore.
        order.verify(semaphore, times(1)).tryAcquire(12, TimeUnit.MILLISECONDS);
        // Then submit the task.
        order.verify(taskExecutor, times(1)).execute(any());
        // Then check that the task is actually executed after submission and before the semaphore is released.
        order.verify(semaphore, times(1)).availablePermits();
        // The last operation should be releasing the semaphore.
        order.verify(semaphore, times(1)).release();
    }
    
    @Test
    public void testSemaphoreCanNotBeAcquired() throws Exception {
        SyncTaskExecutor taskExecutor = spy(new SyncTaskExecutor());
        LimitedCPUUsageExecutorHelper executor= 
            new FourCoreLimitedCPUUsageExecutorHelper(taskExecutor, getConfigWithExtraSearchCpuPercent(0));
        
        // Setup a dummy semaphore.
        Semaphore semaphore = mock(Semaphore.class);
        when(semaphore.tryAcquire(anyLong(), any())).thenReturn(false);
        executor.setSemaphore(semaphore);
        
        for(int i = 0; i < 2; i++) {
            Optional<FutureTask<String>> task = executor.submit(() -> {
                Assert.fail("Should not have executed task as semaphore could not be acquired.");
                return "";
            }, "name", 12);
            
            Assert.assertFalse(task.isPresent());
            
            //Try to submit another task, in this case the semaphore should not be consulted.
        }
        
        //Check that we only tried to wait on the semaphore once (we should not re-try if we ever time out).
        verify(semaphore, times(1)).tryAcquire(anyLong(), any());
    }
    
    @Test
    public void testSemaphoreCanNotBeAcquiredInterupted() throws Exception {
        SyncTaskExecutor taskExecutor = spy(new SyncTaskExecutor());
        LimitedCPUUsageExecutorHelper executor= 
            new FourCoreLimitedCPUUsageExecutorHelper(taskExecutor, getConfigWithExtraSearchCpuPercent(0));
        
        // Setup a dummy semaphore.
        Semaphore semaphore = mock(Semaphore.class);
        when(semaphore.tryAcquire(anyLong(), any())).thenThrow(new InterruptedException());
        executor.setSemaphore(semaphore);
        
        try {
            executor.submit(() -> {
                Assert.fail("Should not have executed task as semaphore could not be acquired.");
                return "";
            }, "name", 12);
            Assert.fail("Interruped exception should have been re-thrown.");
        } catch (RuntimeException e) {
            
        }
        
        Optional<FutureTask<String>> task = executor.submit(() -> {
            Assert.fail("Should not have executed task as semaphore could not be acquired.");
            return "";
        }, "name", 12);
        
        Assert.assertFalse(task.isPresent());
        
        //Check that we only tried to wait on the semaphore once (we should not re-try if we ever time out).
        verify(semaphore, times(1)).tryAcquire(anyLong(), any());
    }
    
    @Test
    public void testSubmissionOfTaskFailed() throws Exception {
        TaskExecutor taskExecutor = mock(TaskExecutor.class);
        doThrow(new TaskRejectedException("")).when(taskExecutor).execute(any());
        
        LimitedCPUUsageExecutorHelper executor= 
            new FourCoreLimitedCPUUsageExecutorHelper(taskExecutor, getConfigWithExtraSearchCpuPercent(0));
        
        // Setup a dummy semaphore.
        Semaphore semaphore = mock(Semaphore.class);
        when(semaphore.tryAcquire(12, TimeUnit.MILLISECONDS)).thenReturn(true);
        executor.setSemaphore(semaphore);
        
        
        Optional<FutureTask<String>> task = executor.submit(() -> {
            Assert.fail("Should not have been executed");
            return "";
        }, "name", 12);
        
        Assert.assertFalse(task.isPresent());
        
        InOrder order = inOrder(semaphore, taskExecutor);
        // First try to take the semaphore.
        order.verify(semaphore, times(1)).tryAcquire(12, TimeUnit.MILLISECONDS);
        // Then submit the task.
        order.verify(taskExecutor, times(1)).execute(any());
        // The last operation should be releasing the semaphore.
        order.verify(semaphore, times(1)).release();
    }
    
    public class RecordingTaskExecutor implements TaskExecutor {

        @Getter List<Runnable> tasks = new ArrayList<>();
        
        @Override
        public void execute(Runnable task) {
            tasks.add(task);
        }
        
    }
    
    ServiceConfigReadOnly getConfigWithExtraSearchCpuPercent(double pc) {
        ServiceConfigReadOnly config = mock(ServiceConfigReadOnly.class);
        when(config.get(FrontEndKeys.ModernUi.EXTRA_SEARCH_CPU_COUNT_PERCENTAGE)).thenReturn(pc);
        return config;
    }
}
