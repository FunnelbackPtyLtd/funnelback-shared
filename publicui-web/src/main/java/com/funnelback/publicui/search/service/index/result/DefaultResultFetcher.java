package com.funnelback.publicui.search.service.index.result;

import com.funnelback.dataapi.connector.padre.PadreConnector;
import com.funnelback.dataapi.connector.padre.docinfo.DocInfo;
import com.funnelback.dataapi.connector.padre.docinfo.DocInfoQuery;
import com.funnelback.publicui.search.model.padre.Result;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URI;
import java.util.List;

/**
 * Fetches result using the Padre Data Api connector.
 */
@Component
@Log4j
public class DefaultResultFetcher implements ResultFetcher {

    /**
     * Will fetch all existing metadata for a given result
     */
    @Override
    public Result fetchResult(File indexStem, URI resultUri) {
        
        List<DocInfo> dis = new PadreConnector(indexStem)
            .docInfo(resultUri)
            .withMetadata(DocInfoQuery.ALL_METADATA)
            .fetch().asList();

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
        r.getMetaData().putAll(di.getMetaData());
        
        return r;        
    }

}
