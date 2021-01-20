package __fixed_package__;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.funnelback.plugin.index.IndexConfigProviderContext;
import com.funnelback.plugin.index.IndexingConfigProvider;
import com.funnelback.plugin.index.consumers.ExternalMetadataConsumer;

public class _ClassNamePrefix_IndexingConfigProvider implements IndexingConfigProvider {

    private static final Logger log = LogManager.getLogger(_ClassNamePrefix_IndexingConfigProvider.class);
    
    @Override
    public void externalMetadata(IndexConfigProviderContext context, ExternalMetadataConsumer consumer) {
        log.trace("This method can be used to supply additional metadata, check IndexingConfigProvider for other methods.");
    }
}
