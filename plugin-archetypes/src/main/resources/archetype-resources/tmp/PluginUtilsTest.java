package ${package};

import com.funnelback.plugin.PluginUtilsBase;
import com.funnelback.plugin.test.AbstractPluginUtilsTest;

public class PluginUtilsTest extends AbstractPluginUtilsTest {

    private final PluginUtils pluginUtils = new PluginUtils();

    @Override
    public PluginUtilsBase getPluginUtils(){
        return pluginUtils;
    }
}
