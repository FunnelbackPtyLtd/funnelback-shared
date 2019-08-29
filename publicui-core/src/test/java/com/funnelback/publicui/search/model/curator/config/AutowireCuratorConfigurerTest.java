package com.funnelback.publicui.search.model.curator.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.function.Consumer;

import org.junit.Test;

import com.funnelback.publicui.search.model.curator.HasNoBeans;

public class AutowireCuratorConfigurerTest {

    @Test
    public void test() {
        Consumer c = mock(Consumer.class);
        new AutowireCuratorConfigurer(c).configure(new DummyHasNoBeans());
        verify(c, never()).accept(any());
        
        new AutowireCuratorConfigurer(c).configure(new DummyHasBeans());
        verify(c, times(1)).accept(any());
    }
    
    public static class DummyHasBeans {
        
    }
    
    public static class DummyHasNoBeans implements HasNoBeans {
        
    }
}
