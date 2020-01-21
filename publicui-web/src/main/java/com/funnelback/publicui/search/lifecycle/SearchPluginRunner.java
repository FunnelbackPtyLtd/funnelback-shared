package com.funnelback.publicui.search.lifecycle;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.funnelback.common.config.CollectionId;
import com.funnelback.config.keys.Keys;
import com.funnelback.plugin.PluginClassLoaderCache;
import com.funnelback.plugin.PluginHelper;
import com.funnelback.plugin.PluginId;
import com.funnelback.plugin.PluginIdAndVersion;
import com.funnelback.plugin.PluginRunner;
import com.funnelback.plugin.PluginVersion;
import com.funnelback.plugin.SearchLifeCyclePlugin;
import com.funnelback.publicui.search.model.collection.Collection.Hook;
import com.funnelback.publicui.search.model.transaction.ExecutionContext;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.utils.web.ExecutionContextHolder;

import lombok.extern.log4j.Log4j2;
import static com.funnelback.publicui.search.lifecycle.GenericHookScriptRunner.appendOnSearchTypeMsg;

@Component
@Log4j2
public class SearchPluginRunner {

    private PluginClassLoaderCache classLoaderCache;
    
    public SearchPluginRunner(@Autowired File searchHome, @Autowired ExecutionContextHolder executionContextHolder) throws Exception {
        this.classLoaderCache = new PluginClassLoaderCache(searchHome,
            c -> {
                // It would be better if we could support marking an entry as stale.
                
                // Under Admin try to be active about reloading the plugin we assume development will happen on this port.
                if(executionContextHolder.getExecutionContext() == ExecutionContext.Admin) {
                    // Only hang on to entries for up to 30s this way it should be easier to develop the plugin
                    // as it will reload quickly after a change.
                    return c.softValues()
                        .expireAfterAccess(15, TimeUnit.MINUTES)
                        .refreshAfterWrite(30, TimeUnit.SECONDS);
                }
                
                // Not an admin port, probably a real search
                return c.softValues()
                    .expireAfterAccess(4, TimeUnit.HOURS)
                    .refreshAfterWrite(3, TimeUnit.MINUTES);
            },
            
            Executors.newCachedThreadPool());
    }
    
    @Scheduled(fixedDelay = 30_1000)
    public void cleanUpCache() {
        
    }
    
    
    public void runPluginsFor(SearchTransaction st, Hook hook) {
        for(PluginId plugin : new PluginHelper().enabledPlugins(st.getQuestion().getCurrentProfileConfig())) {
            log.debug("Running: " + plugin);
            try {
                PluginVersion version = st.getQuestion().getCurrentProfileConfig().get(Keys.CollectionKeys.Plugin.version(plugin));
                PluginIdAndVersion pluginIdAndVersion = new PluginIdAndVersion(plugin, version);
                this.classLoaderCache.withPluginClassLoader(new CollectionId(st.getQuestion().getCollection().getId()),
                    pluginIdAndVersion, 
                    classLoader -> {
                        new PluginRunner()
                        .<SearchLifeCyclePlugin>withSearchPluginB()
                        .pluginClassLoader(classLoader)
                        .pluginIdAndVersion(pluginIdAndVersion)
                        .classType(SearchLifeCyclePlugin.class)
                        .withSearchPlugin(searchPlugin -> {
                            switch (hook) {
                            case extra_searches:
                                // No longer recommended
                                break;
                            case pre_cache:
                                // Not supported in stencils we should, note that it would need to have some sort of return value.
                                break;
                            case pre_process:
                                searchPlugin.preProcess(st);
                                break;
                            case pre_datafetch:
                                searchPlugin.preDatafetch(st);
                                break;
                            case post_datafetch:
                                searchPlugin.postDatafetch(st);
                                break;
                            case post_process:
                                searchPlugin.postProcess(st);
                                break;
                            default:
                                break;
                            }
                            
                        })
                        .onPluginDoesntRunThisClass((m, e) -> {
                            // We don't need to log this, the plugin doesn't deal with the search life cycle and that is ok.
                        })
                        .onError(e -> {
                            log.warn("Error when running plugin: '" + plugin + "'" + appendOnSearchTypeMsg(st), e);
                        })
                        .run();
                        
                        return null;
                    });
            } catch (Exception e) {
                log.warn("Error when running plugin: '" + plugin + "'" + appendOnSearchTypeMsg(st), e);
            }
        }
    }
    
}
