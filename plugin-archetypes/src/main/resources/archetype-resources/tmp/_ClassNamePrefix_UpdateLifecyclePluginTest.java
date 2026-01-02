package ${package};

import com.funnelback.plugin.update.mock.MockUpdateLifecycleContext;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class _ClassNamePrefix_UpdateLifecyclePluginTest {
    private _ClassNamePrefix_UpdateLifecyclePlugin underTest;

    @BeforeEach
    void setup() {
        underTest = new _ClassNamePrefix_UpdateLifecyclePlugin();
    }

    @Test
    void testOnLifecycleEvent() {
        MockUpdateLifecycleContext mockContext = new MockUpdateLifecycleContext();

        // Update this to call the method(s) that should be tested.
        underTest.onPreGather(mockContext);
    }
}