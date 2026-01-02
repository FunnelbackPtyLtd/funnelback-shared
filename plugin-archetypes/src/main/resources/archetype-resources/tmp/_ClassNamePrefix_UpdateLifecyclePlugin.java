package ${package};

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.funnelback.plugin.update.UpdateLifecycleContext;
import com.funnelback.plugin.update.UpdateLifecyclePlugin;

public class _ClassNamePrefix_UpdateLifecyclePlugin implements UpdateLifecyclePlugin {

    final PluginUtils pluginUtils = new PluginUtils();
    private static final Logger log = LogManager.getLogger(_ClassNamePrefix_UpdateLifecyclePlugin.class);

    @Override
    public void onPreGather(UpdateLifecycleContext context) {
        log.trace("Modify this or implement other methods to execute them during collection update lifecycle");
    }
}