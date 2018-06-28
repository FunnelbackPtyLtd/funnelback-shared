package com.funnelback.publicui.search.service.index.result;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import lombok.extern.log4j.Log4j2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.dataapi.connector.padre.PadreConnector;
import com.funnelback.dataapi.connector.padre.docinfo.DocInfo;
import com.funnelback.dataapi.connector.padre.docinfo.DocInfoAccess;
import com.funnelback.publicui.search.model.padre.Result;

/**
 * Fetches result using the Padre Data Api connector.
 */
@Component
@Log4j2
public class DefaultResultFetcher implements ResultFetcher {

    @Autowired
    private File searchHome;
    
    /**
     * Will fetch all existing metadata for a given result
     */
    @Override
    public Result fetchResult(File indexStem, URI resultUri) {
        
        Set<String> metadata = DocInfoAccess.getMetadataForStem(indexStem);
        
        List<DocInfo> dis = new ArrayList<>(new PadreConnector(searchHome, indexStem)
            .docInfo(resultUri)
            .withMetadata(metadata.toArray(new String[0]))
            .fetch().asMap().values());

        if (dis.size() < 1) {
            return null;
        } else if (dis.size() > 1) {
            log.warn("More that one result for URL '"+resultUri+"' from index" +
                "'"+indexStem+"'. Returning the first one.");
        }
        
        // Convert DocInfo result into Modern UI Result
        
        DocInfo di = dis.get(0);
        
        Result r = new Result();
        r.setIndexUrl(di.getUri().toString());
        r.setDate(di.getDate());
        r.setFileType(di.getFileType());
        r.setFileSize(di.getUnfilteredLength());
        r.setSummary(di.getSummaryText());
        r.setTitle(di.getTitle());
        for (Map.Entry<String, List<String>> e : di.getMetaData().entrySet()) {
            r.getMetaData().put(e.getKey(), String.join("|", e.getValue()));
        }
        
        return r;        
    }

}
