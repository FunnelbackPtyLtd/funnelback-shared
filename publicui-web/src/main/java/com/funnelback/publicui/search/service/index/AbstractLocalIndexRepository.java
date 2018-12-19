package com.funnelback.publicui.search.service.index;

import java.util.Date;
import java.util.Map;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.funnelback.common.config.Collection.Type;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Files;
import com.funnelback.common.config.indexer.BuildInfoUtils;
import com.funnelback.common.views.View;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.padre.Details;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.service.IndexRepository;
import com.funnelback.publicui.search.service.index.result.ResultFetcher;

import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/**
 * Implementation that assumes that the indexes are on the local disk.
 */
@Log4j2
public abstract class AbstractLocalIndexRepository implements IndexRepository {

    @Autowired
    @Setter private ConfigRepository configRepository;

    @Autowired
    @Setter private ResultFetcher resultFetcher;
    
    /**
     * Reads the index_time file to get the last update time
     * of the index.
     * @param collectionId
     * @return The updated Date, or null if the date is not available.
     * @throws UnsupportedOperationException If the collection doesn't have a
     * <code>index_time</code> parameter (such as meta collections)
     */
    protected Date loadLastUpdated(String collectionId) throws UnsupportedOperationException {
        Collection c = configRepository.getCollection(collectionId);
        if (c == null) {
            return null;
        }
        if (Type.meta.equals(c.getType())) {
            throw new UnsupportedOperationException("Meta collection don't have an updated time");
        }
        
        try {
            File indexTimeFile = getIndexFile(collectionId, Files.Index.INDEX_TIME);
            if (indexTimeFile.canRead()) {
                String time = FileUtils.readFileToString(indexTimeFile);
                try {
                    return Details.getUpdateDateFormat().parse(time);
                } catch (Exception e) {
                    log.warn("Could not parse last update date string '" + time + "' from '" + indexTimeFile.getAbsolutePath() + "'.", e);
                }
            }
        } catch (IOException ioe) {
            log.error("Could not load last updated date for collection '" + collectionId + "'", ioe);
        }
        
        return null;
    }
    
    /**
     * Loads the <code>.bldinfo</code> file of a collection and returns its
     * content as a Map.
     * @param collectionId
     * @return
     * @throws UnsupportedOperationException If the collection doesn't have a
     * <code>.bldinfo</code> parameter (such as meta collections)
     */
    protected Map<String, String> loadBuildInfo(String collectionId) throws UnsupportedOperationException{
        Collection c = configRepository.getCollection(collectionId);
        if (c == null) {
            return null;
        }
        if (Type.meta.equals(c.getType())) {
            throw new UnsupportedOperationException("Meta collection don't have a build info file");
        }
        
        try {
            File indexBldInfoFile = getIndexFile(collectionId, Files.Index.BLDINFO);
            if (indexBldInfoFile.canRead()) {
                return BuildInfoUtils.loadBuildInfo(indexBldInfoFile);
            }
        } catch (IOException ioe) {
            log.error("Could not load bldinfo file for collection '" + collectionId + "'", ioe);
        }
        
        return null;
    }
    
    protected File getIndexFile(String collectionId, String fileName) {
        return new File(configRepository.getCollection(collectionId).getConfiguration().getCollectionRoot()
                + File.separator + View.live
                + File.separator + DefaultValues.FOLDER_IDX,
                fileName);
    }

    @Override
    public Result getResult(Collection collection, URI indexUri) {
        File indexStem = new File(collection.getConfiguration().getCollectionRoot()
                + File.separator + View.live
                + File.separator + DefaultValues.FOLDER_IDX,
                DefaultValues.INDEXFILES_PREFIX);

        return resultFetcher.fetchResult(indexStem, collection.getConfiguration().getCollectionName(), indexUri);
    }
}
