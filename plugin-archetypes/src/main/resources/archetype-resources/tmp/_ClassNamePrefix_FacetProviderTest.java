package __fixed_package__;

import org.junit.Test;

import com.funnelback.plugin.index.mock.MockIndexConfigProviderContext;

public class _ClassNamePrefix_FacetProviderTest {

    @Test
    public void testFacetsPlugin(){
        MockIndexConfigProviderContext mockContext = new MockIndexConfigProviderContext();
        
        // Call the class.
        new _ClassNamePrefix_FacetProvider().extraFacetedNavigation(mockContext);
    }
    
}
